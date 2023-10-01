package com.kneelawk.packvulcan.net.curseforge

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.curseforge.file.FileJson
import com.kneelawk.packvulcan.model.curseforge.file.GetFileJson
import com.kneelawk.packvulcan.model.curseforge.project.GetModJson
import com.kneelawk.packvulcan.model.curseforge.project.ProjectJson
import com.kneelawk.packvulcan.net.installJson
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

object CurseforgeApi {
    private val HTTP_CLIENT = HttpClient(CIO) {
        installJson()
    }

    private val projectCache: AsyncCache<Long, ProjectJson> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()
    private val fileCache: AsyncCache<FileLookup, FileJson> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()

    private suspend fun retrieveProject(modId: Long): ProjectJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.curseforge.com/v1/mods/$modId") {
            header("x-api-key", CurseforgeKey.CURSEFORGE_KEY)
        }.body<GetModJson>().data
    }

    private suspend fun retrieveFile(lookup: FileLookup): FileJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.curseforge.com/v1/mods/${lookup.modId}/files/${lookup.fileId}") {
            header("x-api-key", CurseforgeKey.CURSEFORGE_KEY)
        }.body<GetFileJson>().data
    }

    suspend fun project(modId: Long): ProjectJson = projectCache.suspendGet(modId, ::retrieveProject)

    suspend fun file(modId: Long, fileId: Long): FileJson =
        fileCache.suspendGet(FileLookup(modId, fileId), ::retrieveFile)

    private data class FileLookup(val modId: Long, val fileId: Long)
}
