package com.kneelawk.packvulcan.net.modrinth

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.model.modrinth.tag.CategoryJson
import com.kneelawk.packvulcan.model.modrinth.tag.LoaderJson
import com.kneelawk.packvulcan.model.modrinth.team.TeamMemberJson
import com.kneelawk.packvulcan.model.modrinth.version.VersionJson
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.Duration

object ModrinthApi {
    private val log = KotlinLogging.logger { }

    private val projectCache: AsyncCache<String, Either<ProjectJson, MissingProject>> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()
    private val versionCache: AsyncCache<String, Either<VersionJson, MissingVersion>> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).buildAsync()
    private val teamMemberCache: AsyncCache<String, List<TeamMemberJson>> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(2)).buildAsync()
    private val categoryCache: AsyncCache<Unit, List<CategoryJson>> =
        Caffeine.newBuilder().buildAsync()
    private val loaderCache: AsyncCache<Unit, List<LoaderJson>> =
        Caffeine.newBuilder().buildAsync()

    /*
     * Retriever methods.
     */

    private val projectBatcher =
        Batcher<String, Either<ProjectJson, MissingProject>>(ApplicationScope, Duration.ofMillis(500), Dispatchers.IO) { requests ->
            val ids = requests.map { it.request }
            log.debug("Project batch request, ids: $ids")

            val result = HTTP_CLIENT.get("https://api.modrinth.com/v2/projects") {
                parameter("ids", Json.encodeToString(ids))
            }.body<List<ProjectJson>>().associateBy { it.id }

            log.debug("Request count: ${ids.size}, response count: ${result.size}")

            for ((id, channel) in requests) {
                channel.send(leftOr(result[id], MissingProject(id)))
            }
        }

    private val versionBatcher =
        Batcher<String, Either<VersionJson, MissingVersion>>(ApplicationScope, Duration.ofMillis(500), Dispatchers.IO) { requests ->
            val ids = requests.map { it.request }
            log.debug("Version batch request, ids: $ids")

            val result = HTTP_CLIENT.get("https://api.modrinth.com/v2/versions") {
                parameter("ids", Json.encodeToString(ids))
            }.body<List<VersionJson>>().associateBy { it.id }

            log.debug("Request count: ${ids.size}, response count: ${result.size}")

            for ((id, channel) in requests) {
                channel.send(leftOr(result[id], MissingVersion(id)))
            }
        }

    private suspend fun retrieveTeamMembers(id: String): List<TeamMemberJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/team/$id/members").body()
    }

    private suspend fun retrieveCategories(): List<CategoryJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/tag/category").body()
    }

    private suspend fun retrieveLoaders(): List<LoaderJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/tag/loader").body()
    }

    /*
     * Accessor methods.
     */

    suspend fun project(idOrSlug: String): Either<ProjectJson, MissingProject> = projectCache.suspendGet(idOrSlug, projectBatcher::request)

    suspend fun version(id: String): Either<VersionJson, MissingVersion> = versionCache.suspendGet(id, versionBatcher::request)

    suspend fun teamMembers(id: String): List<TeamMemberJson> = teamMemberCache.suspendGet(id, ::retrieveTeamMembers)

    suspend fun categories(): List<CategoryJson> = categoryCache.suspendGet(Unit) { retrieveCategories() }

    suspend fun loaders(): List<LoaderJson> = loaderCache.suspendGet(Unit) { retrieveLoaders() }
}
