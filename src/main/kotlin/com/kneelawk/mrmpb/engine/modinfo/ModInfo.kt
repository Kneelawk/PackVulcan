package com.kneelawk.mrmpb.engine.modinfo

import com.kneelawk.mrmpb.engine.packwiz.PackwizMetaFile
import com.kneelawk.mrmpb.model.FullModInfo
import com.kneelawk.mrmpb.model.ModIcon
import com.kneelawk.mrmpb.model.packwiz.mod.ModToml
import com.kneelawk.mrmpb.model.packwiz.mod.ModrinthToml
import com.kneelawk.mrmpb.net.modrinth.ModrinthApi

object ModInfo {
    suspend fun getFullInfo(mod: PackwizMetaFile): FullModInfo {
        val modToml = mod.toml
        return modToml.update?.modrinth?.let { modrinth ->
            getModrinthInfo(modToml, modrinth)
        } ?: TODO("Non-modrinth mod support")
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

//    private suspend fun getFileInfo(mod: ModToml): FullModInfo {
//        val file = ModFileCache.getModFile(mod.download.url, mod.download.hash, mod.download.hashFormat)
//        val modFileSystem = FileSystem.SYSTEM.openZip(file.toOkioPath())
//
//
//    }

    private fun authorString(authors: List<String>): String {
        return when {
            authors.isEmpty() -> "Unknown"
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