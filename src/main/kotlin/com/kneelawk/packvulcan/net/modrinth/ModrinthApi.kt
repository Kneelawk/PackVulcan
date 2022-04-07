package com.kneelawk.packvulcan.net.modrinth

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.model.modrinth.team.TeamMemberJson
import com.kneelawk.packvulcan.model.modrinth.version.VersionJson
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.util.ApplicationScope
import com.kneelawk.packvulcan.util.Batcher
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.Duration

object ModrinthApi {
    private val log = KotlinLogging.logger { }

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
        Batcher<String, ProjectJson>(ApplicationScope, Duration.ofMillis(500), Dispatchers.IO) { requests ->
            val ids = requests.map { it.request }
            log.debug("Project batch request, ids: $ids")

            val result: List<ProjectJson> = HTTP_CLIENT.get("https://api.modrinth.com/v2/projects") {
                parameter("ids", Json.encodeToString(ids))
            }

            assert(
                result.size == requests.size
            ) { "Received different number of projects than requested. Request count: ${requests.size}, result count: ${result.size}" }

            for (index in result.indices) {
                requests[index].responseChannel.send(result[index])
            }
        }

    private val versionBatcher =
        Batcher<String, VersionJson>(ApplicationScope, Duration.ofMillis(500), Dispatchers.IO) { requests ->
            val ids = requests.map { it.request }
            log.debug("Version batch request, ids: $ids")

            val result: List<VersionJson> = HTTP_CLIENT.get("https://api.modrinth.com/v2/versions") {
                parameter("ids", Json.encodeToString(ids))
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