package com.kneelawk.mrmpb.net.modrinth

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.modrinth.project.ProjectJson
import com.kneelawk.mrmpb.model.modrinth.team.TeamMemberJson
import com.kneelawk.mrmpb.model.modrinth.version.VersionJson
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import com.kneelawk.mrmpb.util.suspendGet
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private suspend fun retrieveProject(idOrSlug: String): ProjectJson = withContext(Dispatchers.IO) {
        // TODO: lookup batching using https://api.modrinth.com/v2/projects?ids=[%22P7dR8mSH%22,%22Ha28R6CL%22]
        HTTP_CLIENT.get("https://api.modrinth.com/v2/project/$idOrSlug")
    }

    private suspend fun retrieveVersion(id: String): VersionJson = withContext(Dispatchers.IO) {
        // TODO: lookup batching using https://api.modrinth.com/v2/versions?ids=[%221bDn0oLI%22,%22pKzU4NF4%22]
        HTTP_CLIENT.get("https://api.modrinth.com/v2/version/$id")
    }

    private suspend fun retrieveTeamMembers(id: String): List<TeamMemberJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://api.modrinth.com/v2/team/$id/members")
    }

    /*
     * Accessor methods.
     */

    suspend fun project(idOrSlug: String): ProjectJson = projectCache.suspendGet(idOrSlug, ::retrieveProject)

    suspend fun version(id: String): VersionJson = versionCache.suspendGet(id, ::retrieveVersion)

    suspend fun teamMembers(id: String): List<TeamMemberJson> = teamMemberCache.suspendGet(id, ::retrieveTeamMembers)
}