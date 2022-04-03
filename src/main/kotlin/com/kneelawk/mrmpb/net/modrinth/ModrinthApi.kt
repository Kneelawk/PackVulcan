package com.kneelawk.mrmpb.net.modrinth

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.modrinth.project.ProjectJson
import com.kneelawk.mrmpb.model.modrinth.team.TeamMemberJson
import com.kneelawk.mrmpb.model.modrinth.version.VersionJson
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import com.kneelawk.mrmpb.util.ApplicationScope
import com.kneelawk.mrmpb.util.Batcher
import com.kneelawk.mrmpb.util.suspendGet
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration

object ModrinthApi {
    private val projectCache: AsyncCache<String, ProjectJson> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()
    private val versionCache: AsyncCache<String, VersionJson> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()
    private val teamMemberCache: AsyncCache<String, List<TeamMemberJson>> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(2)).buildAsync()

    /*
     * Retriever methods.
     */

    private val projectBatcher =
        Batcher<String, ProjectJson>(ApplicationScope, Duration.ofSeconds(1), Dispatchers.IO) { requests ->
            val result: List<ProjectJson> = HTTP_CLIENT.get("https://api.modrinth.com/v2/projects") {
                parameter("ids", Json.encodeToString(requests.map { it.request }))
            }

            assert(
                result.size == requests.size
            ) { "Received different number of projects than requested. Request count: ${requests.size}, result count: ${result.size}" }

            for (index in result.indices) {
                requests[index].responseChannel.send(result[index])
            }
        }

    private val versionBatcher =
        Batcher<String, VersionJson>(ApplicationScope, Duration.ofSeconds(1), Dispatchers.IO) { requests ->
            val result: List<VersionJson> = HTTP_CLIENT.get("https://api.modrinth.com/v2/versions") {
                parameter("ids", Json.encodeToString(requests.map { it.request }))
            }

            assert(
                result.size == requests.size
            ) { "Received different number of versions than requested. Request count: ${requests.size}, result count: ${result.size}" }

            for (index in result.indices) {
                requests[index].responseChannel.send(result[index])
            }
        }

    private suspend fun retrieveTeamMembers(id: String): List<TeamMemberJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/team/$id/members")
    }

    /*
     * Accessor methods.
     */

    suspend fun project(idOrSlug: String): ProjectJson = projectCache.suspendGet(idOrSlug, projectBatcher::request)

    suspend fun version(id: String): VersionJson = versionCache.suspendGet(id, versionBatcher::request)

    suspend fun teamMembers(id: String): List<TeamMemberJson> = teamMemberCache.suspendGet(id, ::retrieveTeamMembers)
}