package com.kneelawk.packvulcan.model

import com.kneelawk.packvulcan.engine.packwiz.PackwizMetaFile
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.engine.packwiz.PackwizModFile
import com.kneelawk.packvulcan.model.packwiz.Side
import com.kneelawk.packvulcan.model.packwiz.mod.*
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

sealed interface SimpleModInfo {
    val name: String
    val author: String
    val filename: String
    val version: String
    val description: String?
    val icon: ModIconSource?
    val projectUrl: String?

    fun toPackwizMod(
        modpackLocation: Path, modsPathStr: String, metafileExtension: String, alias: String?, preserve: Boolean
    ): PackwizMod

    data class Modrinth(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIconSource?,
        override val projectUrl: String, val projectId: String, val versionId: String, val slug: String, val side: Side,
        val downloadUrl: String, val sha1: String, val sha512: String
    ) : SimpleModInfo {
        override fun toPackwizMod(
            modpackLocation: Path, modsPathStr: String, metafileExtension: String, alias: String?, preserve: Boolean
        ): PackwizMod {
            return PackwizMetaFile(
                "$modsPathStr/$slug$metafileExtension",
                alias,
                preserve,
                ModToml(
                    name, filename, side, DownloadToml(downloadUrl, HashFormat.SHA512, sha512, ""), null,
                    UpdateToml(modrinth = ModrinthToml(projectId, versionId))
                ),
                this
            )
        }
    }

    data class Curseforge(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIconSource?,
        override val projectUrl: String, val projectId: Long, val fileId: Long, val slug: String, val side: Side,
        val sha1: String
    ) : SimpleModInfo {
        override fun toPackwizMod(
            modpackLocation: Path, modsPathStr: String, metafileExtension: String, alias: String?, preserve: Boolean
        ): PackwizMod {
            return PackwizMetaFile(
                "$modsPathStr/$slug$metafileExtension",
                alias,
                preserve,
                ModToml(
                    name, filename, side, DownloadToml(null, HashFormat.SHA1, sha1, "metadata:curseforge"), null,
                    UpdateToml(curseforge = CurseforgeToml(fileId, projectId))
                ),
                this
            )
        }
    }

    data class Url(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIconSource?,
        override val projectUrl: String?, val modId: String, val downloadUrl: String, val hashFormat: HashFormat,
        val hash: String, val metaFilePath: String, val side: Side
    ) : SimpleModInfo {
        override fun toPackwizMod(
            modpackLocation: Path, modsPathStr: String, metafileExtension: String, alias: String?, preserve: Boolean
        ): PackwizMod {
            return PackwizMetaFile(
                metaFilePath,
                alias,
                preserve,
                ModToml(
                    name, filename, side, DownloadToml(downloadUrl, hashFormat, hash, ""), null, null
                ),
                this
            )
        }
    }

    data class File(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIconSource?,
        override val projectUrl: String?, val modId: String, val path: Path
    ) : SimpleModInfo {
        override fun toPackwizMod(
            modpackLocation: Path, modsPathStr: String, metafileExtension: String, alias: String?, preserve: Boolean
        ): PackwizMod {
            val relative = path.relativeTo(modpackLocation).invariantSeparatorsPathString

            return PackwizModFile(
                relative,
                alias,
                preserve,
                path,
                this
            )
        }
    }
}
