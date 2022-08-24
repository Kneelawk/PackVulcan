package com.kneelawk.packvulcan.engine.modinfo

import com.kneelawk.packvulcan.engine.packwiz.PackwizMetaFile
import com.kneelawk.packvulcan.model.ModIconSource
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.packwiz.mod.ModToml
import com.kneelawk.packvulcan.model.packwiz.mod.ModrinthToml
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi

object ModInfo {
    fun getModProvider(mod: PackwizMetaFile): ModProvider {
        return when {
            mod.toml.update?.modrinth != null -> ModProvider.MODRINTH
            mod.toml.download.mode == "metadata:curseforge" -> ModProvider.CURSEFORGE
            else -> ModProvider.URL
        }
    }

    suspend fun getSimpleInfo(mod: PackwizMetaFile): SimpleModInfo? {
        val modToml = mod.toml
        return modToml.update?.modrinth?.let { modrinth ->
            getModrinthInfo(modToml, modrinth)
        } ?: ModFileInfo.getFileInfo(modToml)
    }

    private suspend fun getModrinthInfo(mod: ModToml, modrinth: ModrinthToml): SimpleModInfo? {
        val project = ModrinthApi.project(modrinth.modId).escapeIfRight { return null }
        val version = ModrinthApi.version(modrinth.version).escapeIfRight { return null }
        val teamMembers = ModrinthApi.teamMembers(project.team)

        val authors = authorString(teamMembers.map { it.user.username })
        val projectUrl = "https://modrinth.com/mod/${project.slug}"

        return SimpleModInfo.Modrinth(
            mod.name, authors, mod.filename, version.versionNumber, project.description,
            project.iconUrl?.let { ModIconSource.Url(it) }, projectUrl, modrinth.modId, modrinth.version
        )
    }

    fun authorString(authors: List<String>?): String {
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
