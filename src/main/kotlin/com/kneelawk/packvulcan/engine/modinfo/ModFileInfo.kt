package com.kneelawk.packvulcan.engine.modinfo

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.engine.mod.ModFileCache
import com.kneelawk.packvulcan.engine.packwiz.PackwizMetaFile
import com.kneelawk.packvulcan.model.ModIconSource
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.modfile.IconJson
import com.kneelawk.packvulcan.model.modfile.fabric.FabricModJson
import com.kneelawk.packvulcan.model.modfile.forge.ModsToml
import com.kneelawk.packvulcan.model.modfile.quilt.QuiltModJson
import com.kneelawk.packvulcan.model.packwiz.TomlHelper
import com.kneelawk.packvulcan.model.packwiz.mod.ModToml
import com.kneelawk.packvulcan.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.openZip
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.name
import kotlin.math.abs

object ModFileInfo {
    private const val FABRIC_METADATA_FILE = "/fabric.mod.json"
    private const val FORGE_METADATA_FILE = "/META-INF/mods.toml"
    private const val QUILT_METADATA_FILE = "/quilt.mod.json"

    private val FABRIC_METADATA_PATH = FABRIC_METADATA_FILE.toPath()
    private val FORGE_METADATA_PATH = FORGE_METADATA_FILE.toPath()
    private val QUILT_METADATA_PATH = QUILT_METADATA_FILE.toPath()

    private val modFileInfoCache: AsyncCache<String, Info?> =
        Caffeine.newBuilder().maximumSize(500).buildAsync()

    private val modDecoderJson = Json {
        ignoreUnknownKeys = true
    }

    class Info(val path: Path, val metadata: Metadata, val icon: BufferedImage?)

    sealed class Metadata {
        data class Fabric(val data: FabricModJson) : Metadata()
        data class Forge(val data: ModsToml) : Metadata()
        data class Quilt(val data: QuiltModJson) : Metadata()
    }

    suspend fun getFileInfo(packwizMod: PackwizMetaFile): SimpleModInfo? {
        val mod = packwizMod.toml
        val url = mod.download.url ?: return null
        val modFileInfo = modFileInfoCache.suspendGet(url) {
            loadFileInfo(mod)
        } ?: return null

        val file = getFileInfo(modFileInfo, mod.name, mod.filename)

        return SimpleModInfo.Url(
            file.name, file.author, file.filename, file.version, file.description, file.icon, file.projectUrl,
            file.modId, url, mod.download.hashFormat, mod.download.hash, packwizMod.filePath, mod.side
        )
    }

    suspend fun getFileInfo(path: Path): SimpleModInfo? {
        val info = loadFileInfo(path) ?: return null
        return getFileInfo(info, path.name, path.name)
    }

    private suspend fun getFileInfo(
        info: Info, name: String, filename: String
    ): SimpleModInfo.File {
        val icon = info.icon?.let { original ->
            val bi = if (
                original.width > ImageUtils.MOD_ICON_SIZE ||
                original.height > ImageUtils.MOD_ICON_SIZE ||
                (original.width < ImageUtils.MOD_ICON_SIZE && original.height < ImageUtils.MOD_ICON_SIZE)
            ) {
                ImageUtils.scaleImage(original, ImageUtils.MOD_ICON_SIZE)
            } else {
                original
            }

            ModIconSource.Buffered(bi)
        }

        return when (val meta = info.metadata) {
            is Metadata.Fabric -> {
                val authors = ModInfo.authorString(meta.data.authors?.map { it.name })
                val contact = meta.data.contact

                SimpleModInfo.File(
                    meta.data.name ?: name, authors, filename, meta.data.version, meta.data.description, icon,
                    contact?.get("homepage") ?: contact?.get("sources"), meta.data.id, info.path
                )
            }

            is Metadata.Forge -> {
                val modToml = meta.data.mods.firstOrNull()

                SimpleModInfo.File(
                    modToml?.displayName ?: name, modToml?.authors ?: "Unknown", filename,
                    modToml?.version ?: "unknown", modToml?.description, icon, modToml?.displayURL,
                    modToml?.modId ?: "unknown", info.path
                )
            }

            is Metadata.Quilt -> {
                val quiltLoader = meta.data.quiltLoader
                val metadata = quiltLoader.metadata

                val authors = ModInfo.authorString(metadata?.contributors?.keys?.toList())
                val contact = metadata?.contact

                SimpleModInfo.File(
                    metadata?.name ?: name, authors, filename, quiltLoader.version, metadata?.description,
                    icon, contact?.get("homepage") ?: contact?.get("sources"), quiltLoader.id, info.path
                )
            }
        }
    }

    private suspend fun loadFileInfo(mod: ModToml): Info? {
        val url = mod.download.url ?: return null
        val file = ModFileCache.getModFile(url, mod.download.hash, mod.download.hashFormat)
        return loadFileInfo(file)
    }

    private suspend fun loadFileInfo(file: Path): Info? {
        val modFileSystem = withContext(Dispatchers.IO) {
            try {
                FileSystem.SYSTEM.openZip(file.toOkioPath())
            } catch (e: IOException) {
                null
            }
        } ?: return null

        return when {
            modFileSystem.exists(FABRIC_METADATA_PATH) -> {
                val data = withContext(Dispatchers.IO) {
                    modFileSystem.read(FABRIC_METADATA_PATH) {
                        modDecoderJson.decodeFromString<FabricModJson>(readString(Charsets.UTF_8))
                    }
                }

                val icon = getModFileIcon(data.icon, modFileSystem)

                Info(file, Metadata.Fabric(data), icon)
            }

            modFileSystem.exists(QUILT_METADATA_PATH) -> {
                val data = withContext(Dispatchers.IO) {
                    modFileSystem.read(QUILT_METADATA_PATH) {
                        modDecoderJson.decodeFromString<QuiltModJson>(readString(Charsets.UTF_8))
                    }
                }

                val icon = getModFileIcon(data.quiltLoader.metadata?.icon, modFileSystem)

                Info(file, Metadata.Quilt(data), icon)
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

                Info(file, Metadata.Forge(data), icon)
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
}
