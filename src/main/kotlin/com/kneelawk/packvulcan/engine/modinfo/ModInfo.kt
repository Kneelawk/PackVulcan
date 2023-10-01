package com.kneelawk.packvulcan.engine.modinfo

import com.kneelawk.packvulcan.engine.curseforge.CurseforgeUtils
import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import com.kneelawk.packvulcan.engine.packwiz.PackwizMetaFile
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.SimpleModInfo

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
        return modToml.update?.let { update ->
            update.modrinth?.let { modrinth ->
                ModrinthUtils.getModrinthInfo(modrinth.modId, modrinth.version)
            } ?: update.curseforge?.let { curseforge ->
                CurseforgeUtils.getCurseforgeInfo(curseforge.projectId, curseforge.fileId)
            }
        } ?: ModFileInfo.getFileInfo(mod)
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
