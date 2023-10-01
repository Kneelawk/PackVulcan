package com.kneelawk.packvulcan.engine.curseforge

import com.kneelawk.packvulcan.engine.modinfo.ModInfo
import com.kneelawk.packvulcan.model.ModIconSource
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.curseforge.file.FileJson
import com.kneelawk.packvulcan.model.curseforge.file.HashAlgoJson
import com.kneelawk.packvulcan.model.curseforge.project.ProjectJson
import com.kneelawk.packvulcan.model.packwiz.Side
import com.kneelawk.packvulcan.net.curseforge.CurseforgeApi
import com.kneelawk.packvulcan.net.curseforge.CurseforgeKey

object CurseforgeUtils {
    suspend fun getCurseforgeInfo(projectId: Long, fileId: Long): SimpleModInfo.Curseforge? {
        if (!CurseforgeKey.CURSEFORGE_ENABLED) return null

        val projectJson: ProjectJson
        val fileJson: FileJson

        try {
            projectJson = CurseforgeApi.project(projectId)
            fileJson = CurseforgeApi.file(projectId, fileId)
        } catch (e: Exception) {
            return null
        }

        return SimpleModInfo.Curseforge(
            name = projectJson.name,
            author = ModInfo.authorString(projectJson.authors.map { it.name }),
            filename = fileJson.fileName,
            version = fileJson.displayName,
            description = projectJson.summary,
            icon = ModIconSource.Url(projectJson.logo.url),
            projectUrl = projectJson.links.websiteUrl,
            projectId = projectId,
            fileId = fileId,
            slug = projectJson.slug,
            side = getSide(fileJson.gameVersions),
            sha1 = fileJson.hashes.firstOrNull { it.algo == HashAlgoJson.SHA1 }?.value ?: throw IllegalStateException(
                "Curseforge file ${fileJson.fileName} missing sha1 hash"
            )
        )
    }

    private fun getSide(versions: List<String>): Side {
        var client = false
        var server = false

        for (version in versions) {
            if (version.lowercase() == "client") {
                client = true
            }
            if (version.lowercase() == "server") {
                server = true
            }
        }

        return if (client) {
            if (server) {
                Side.BOTH
            } else {
                Side.CLIENT
            }
        } else {
            if (server) {
                Side.SERVER
            } else {
                Side.BOTH
            }
        }
    }
}
