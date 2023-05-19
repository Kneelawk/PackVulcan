package com.kneelawk.packvulcan.net.modrinth

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.model.modrinth.search.query.SearchQuery
import com.kneelawk.packvulcan.model.modrinth.search.result.SearchResultJson
import com.kneelawk.packvulcan.model.modrinth.tag.CategoryJson
import com.kneelawk.packvulcan.model.modrinth.tag.LoaderJson
import com.kneelawk.packvulcan.model.modrinth.team.TeamMemberJson
import com.kneelawk.packvulcan.model.modrinth.version.query.HashLookup
import com.kneelawk.packvulcan.model.modrinth.version.query.UpdateByHash
import com.kneelawk.packvulcan.model.modrinth.version.query.VersionsQuery
import com.kneelawk.packvulcan.model.modrinth.version.result.VersionJson
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.util.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
    private val versionListCache: AsyncCache<VersionsQuery, List<VersionJson>> =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).buildAsync()

    /*
     * Retriever methods.
     */

    private val projectBatcher = Batcher<String, Either<ProjectJson, MissingProject>>(
        ApplicationScope, Duration.ofMillis(500), Dispatchers.IO
    ) { requests ->
        val ids = requests.map { it.request }
        log.debug("Project batch request, ids: {}", ids)

        val result = HTTP_CLIENT.get("https://api.modrinth.com/v2/projects") {
            parameter("ids", Json.encodeToString(ids))
        }.body<List<ProjectJson>>().associateBy { it.id }

        log.debug("Project request count: {} ({}), response count: {}", ids.size, ids, result.size)

        for ((id, channel) in requests) {
            channel.send(Result.success(leftOr(result[id], MissingProject(id))))
        }
    }

    private val versionBatcher = Batcher<String, Either<VersionJson, MissingVersion>>(
        ApplicationScope, Duration.ofMillis(500), Dispatchers.IO
    ) { requests ->
        val ids = requests.map { it.request }
        log.debug("Version batch request, ids: {}", ids)

        val result = HTTP_CLIENT.get("https://api.modrinth.com/v2/versions") {
            parameter("ids", Json.encodeToString(ids))
        }.body<List<VersionJson>>().associateBy { it.id }

        log.debug("Version request count: {} ({}), response count: {}", ids.size, ids, result.size)

        for ((id, channel) in requests) {
            channel.send(Result.success(leftOr(result[id], MissingVersion(id))))
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

    suspend fun search(query: SearchQuery): SearchResultJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/search") {
            with(url.parameters) {
                query.query?.let { append("query", it) }
                query.facets?.let { andList ->
                    append("facets", Json.encodeToString(andList))
                }
                query.index?.let { append("index", it.apiName) }
                query.offset?.let { append("offset", it.toString()) }
                query.limit?.let { append("limit", it.toString()) }
            }
        }.body()
    }

    private suspend fun retrieveVersions(query: VersionsQuery): List<VersionJson> =
        withContext(Dispatchers.IO) {
            HTTP_CLIENT.get("https://api.modrinth.com/v2/project/${query.projectIdOrSlug}/version") {
                with(url.parameters) {
                    if (query.loaders.isNotEmpty()) {
                        append("loaders", Json.encodeToString(query.loaders))
                    }
                    if (query.gameVersions.isNotEmpty()) {
                        append("game_versions", Json.encodeToString(query.gameVersions))
                    }
                    query.featured?.let { append("featured", it.toString()) }
                }
            }.body()
        }

    suspend fun hashLookup(lookup: HashLookup): Map<String, VersionJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.post("https://api.modrinth.com/v2/version_files") {
            contentType(ContentType.Application.Json)
            setBody(lookup)
        }.body()
    }

    suspend fun updateByHash(update: UpdateByHash): Map<String, VersionJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.post("https://api.modrinth.com/v2/version_files/update") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.body()
    }

    /*
     * Accessor methods.
     */

    suspend fun project(idOrSlug: String): Either<ProjectJson, MissingProject> =
        projectCache.suspendGet(idOrSlug, projectBatcher::request)

    suspend fun version(id: String): Either<VersionJson, MissingVersion> =
        versionCache.suspendGet(id, versionBatcher::request)

    suspend fun teamMembers(id: String): List<TeamMemberJson> = teamMemberCache.suspendGet(id, ::retrieveTeamMembers)

    suspend fun categories(): List<CategoryJson> = categoryCache.suspendGet(Unit) { retrieveCategories() }

    suspend fun loaders(): List<LoaderJson> = loaderCache.suspendGet(Unit) { retrieveLoaders() }

    suspend fun versions(query: VersionsQuery): List<VersionJson> =
        versionListCache.suspendGet(query, ::retrieveVersions)
}
