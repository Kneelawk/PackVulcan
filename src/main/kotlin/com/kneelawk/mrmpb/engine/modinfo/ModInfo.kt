package com.kneelawk.mrmpb.engine.modinfo

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.engine.image.ImageUtils
import com.kneelawk.mrmpb.engine.mod.ModFileCache
import com.kneelawk.mrmpb.engine.packwiz.PackwizMetaFile
import com.kneelawk.mrmpb.model.FullModInfo
import com.kneelawk.mrmpb.model.ModIcon
import com.kneelawk.mrmpb.model.modfile.IconJson
import com.kneelawk.mrmpb.model.modfile.fabric.FabricModJson
import com.kneelawk.mrmpb.model.modfile.forge.ModsToml
import com.kneelawk.mrmpb.model.modfile.quilt.QuiltModJson
import com.kneelawk.mrmpb.model.packwiz.TomlHelper
import com.kneelawk.mrmpb.model.packwiz.mod.ModToml
import com.kneelawk.mrmpb.model.packwiz.mod.ModrinthToml
import com.kneelawk.mrmpb.net.modrinth.ModrinthApi
import com.kneelawk.mrmpb.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.openZip
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.abs

object ModInfo {
    private const val FABRIC_METADATA_FILE = "/fabric.mod.json"
    private const val FORGE_METADATA_FILE = "/META-INF/mods.toml"
    private const val QUILT_METADATA_FILE = "/quilt.mod.json"

    private val FABRIC_METADATA_PATH = FABRIC_METADATA_FILE.toPath()
    private val FORGE_METADATA_PATH = FORGE_METADATA_FILE.toPath()
    private val QUILT_METADATA_PATH = QUILT_METADATA_FILE.toPath()

    private val modFileInfoCache: AsyncCache<String, ModFileInfo?> =
        Caffeine.newBuilder().maximumSize(500).buildAsync()

    private val modDecoderJson = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getFullInfo(mod: PackwizMetaFile): FullModInfo? {
        val modToml = mod.toml
        return modToml.update?.modrinth?.let { modrinth ->
            getModrinthInfo(modToml, modrinth)
        } ?: getFileInfo(modToml)
    }

    private suspend fun getModrinthInfo(mod: ModToml, modrinth: ModrinthToml): FullModInfo {
        val project = ModrinthApi.project(modrinth.modId)
        val version = ModrinthApi.version(modrinth.version)
        val teamMembers = ModrinthApi.teamMembers(project.team)

        val authors = authorString(teamMembers.map { it.user.name ?: it.user.username })
        val projectUrl = "https://modrinth.com/mod/${project.slug}"

        return FullModInfo.Modrinth(
            mod.name, authors, mod.filename, version.name, project.iconUrl?.let { ModIcon.Url(it) }, projectUrl,
            modrinth.modId, modrinth.version
        )
    }

    private suspend fun getFileInfo(mod: ModToml): FullModInfo? {
        val modFileInfo = modFileInfoCache.suspendGet(mod.download.url) {
            loadFileInfo(mod)
        }

        val icon = modFileInfo?.icon?.let { original ->
            val bi = if (
                original.width > ImageUtils.MOD_ICON_SIZE ||
                original.height > ImageUtils.MOD_ICON_SIZE ||
                (original.width < ImageUtils.MOD_ICON_SIZE && original.height < ImageUtils.MOD_ICON_SIZE)
            ) {
                ImageUtils.scaleImage(original, ImageUtils.MOD_ICON_SIZE)
            } else {
                original
            }

            ModIcon.Buffered(bi)
        }

        return when (val meta = modFileInfo?.metadata) {
            is ModFileMetadata.Fabric -> {
                val authors = authorString(meta.data.authors?.map { it.name })

                FullModInfo.File(
                    mod.name, authors, mod.filename, meta.data.version, icon, meta.data.contact?.get("homepage"),
                    meta.data.id
                )
            }
            is ModFileMetadata.Forge -> {
                val modToml = meta.data.mods.firstOrNull()

                FullModInfo.File(
                    mod.name, modToml?.authors ?: "Unknown", mod.filename, modToml?.version ?: "unknown", icon,
                    modToml?.displayURL, modToml?.modId ?: "unknown"
                )
            }
            is ModFileMetadata.Quilt -> {
                val quiltLoader = meta.data.quiltLoader

                val authors = authorString(quiltLoader.metadata?.contributors?.keys?.toList())

                FullModInfo.File(
                    mod.name, authors, mod.filename, quiltLoader.version, icon,
                    quiltLoader.metadata?.contact?.get("homepage"), quiltLoader.id
                )
            }
            null -> null
        }
    }

    private suspend fun loadFileInfo(mod: ModToml): ModFileInfo? {
        val file = ModFileCache.getModFile(mod.download.url, mod.download.hash, mod.download.hashFormat)
        val modFileSystem = withContext(Dispatchers.IO) { FileSystem.SYSTEM.openZip(file.toOkioPath()) }

        return when {
            modFileSystem.exists(FABRIC_METADATA_PATH) -> {
                val data = withContext(Dispatchers.IO) {
                    modFileSystem.read(FABRIC_METADATA_PATH) {
                        modDecoderJson.decodeFromString<FabricModJson>(readString(Charsets.UTF_8))
                    }
                }

                val icon = getModFileIcon(data.icon, modFileSystem)

                ModFileInfo(ModFileMetadata.Fabric(data), icon)
            }
            modFileSystem.exists(QUILT_METADATA_PATH) -> {
                val data = withContext(Dispatchers.IO) {
                    modFileSystem.read(QUILT_METADATA_PATH) {
                        modDecoderJson.decodeFromString<QuiltModJson>(readString(Charsets.UTF_8))
                    }
                }

                val icon = getModFileIcon(data.quiltLoader.metadata?.icon, modFileSystem)

                ModFileInfo(ModFileMetadata.Quilt(data), icon)
            }
            modFileSystem.exists(FORGE_METADATA_PATH) -> {
                val data = withContext(Dispatchers.IO) {
                    modFileSystem.source(FORGE_METADATA_PATH).use {
                        TomlHelper.read(ModsToml, it)
                    }
                }

                val icon = data.mods.firstOrNull()?.logoFile?.let { path ->
                    withContext(Dispatchers.IO) {
                        modFileSystem.read(path.toPath()) {
                            ImageIO.read(inputStream())
                        }
                    }
                }

                ModFileInfo(ModFileMetadata.Forge(data), icon)
            }
            else -> null
        }
    }

    private suspend fun getModFileIcon(iconJson: IconJson?, modFileSystem: FileSystem): BufferedImage? {
        val iconPath = when (iconJson) {
            is IconJson.Multiple -> {
                // find the largest (and hopefully highest-quality) mod icon
                iconJson.paths.keys.minByOrNull { key ->
                    key.toIntOrNull()?.let { abs(it - ImageUtils.MOD_ICON_SIZE) } ?: 4096
                }?.let { iconJson.paths[it] }
            }
            is IconJson.Single -> iconJson.path
            null -> null
        }

        return iconPath?.let { path ->
            withContext(Dispatchers.IO) {
                val okioPath = path.toPath()
                if (modFileSystem.exists(okioPath)) {
                    modFileSystem.read(okioPath) {
                        ImageIO.read(inputStream())
                    }
                } else {
                    null
                }
            }
        }
    }

    private fun authorString(authors: List<String>?): String {
        return when {
            authors == null || authors.isEmpty() -> "Unknown"
            authors.size == 1 -> authors.first()
            authors.size == 2 -> "${authors[0]} and ${authors[1]}"
            else -> {
                val sb = StringBuilder()
                for (index in 0 until (authors.size - 1)) {
                    sb.append(authors[index]).append(", ")
                }
                sb.append("and ").append(authors.last())
                sb.toString()
            }
        }
    }
}