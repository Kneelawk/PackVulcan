package com.kneelawk.packvulcan.engine.modrinth

import com.kneelawk.packvulcan.engine.modinfo.ModInfo
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.model.SimpleModFileInfo
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.model.modrinth.version.result.DependencyJson
import com.kneelawk.packvulcan.model.modrinth.version.result.FileJson
import com.kneelawk.packvulcan.model.modrinth.version.result.VersionJson
import com.kneelawk.packvulcan.model.packwiz.Side
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.util.EMPTY_PROGRESS_LISTENER
import com.kneelawk.packvulcan.util.ProgressListener
import com.kneelawk.packvulcan.util.requireNotNull
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object ModrinthUtils {
    suspend fun getModrinthFileInfo(versionId: String): SimpleModFileInfo.Modrinth? {
        val version = ModrinthApi.version(versionId).escapeIfRight { return null }
        val project = ModrinthApi.project(version.projectId).escapeIfRight { return null }
        return getModrinthFileInfo(project, version)
    }

    suspend fun getModrinthFileInfo(projectIdOrSlug: String, versionId: String): SimpleModFileInfo.Modrinth? {
        val project = ModrinthApi.project(projectIdOrSlug).escapeIfRight { return null }
        val version = ModrinthApi.version(versionId).escapeIfRight { return null }
        return getModrinthFileInfo(project, version)
    }

    suspend fun getModrinthFileInfo(project: ProjectJson, version: VersionJson): SimpleModFileInfo.Modrinth {
        val teamMembers = ModrinthApi.teamMembers(project.team)

        val authors = ModInfo.authorString(teamMembers.map { it.user.username })
        val projectUrl = "https://modrinth.com/mod/${project.slug}"

        val file = getPrimaryFile(version).requireNotNull(
            "Encountered version ${version.versionNumber} of ${project.title} with no files!"
        )

        return SimpleModFileInfo.Modrinth(
            project.title, authors, file.filename, version.versionNumber, project.description,
            project.iconUrl?.let { IconSource.Url(it) }, projectUrl, project.id, version.id, project.slug,
            project.body,
            Side.fromSideCompat(project.clientSide, project.serverSide), file.url, file.hashes.sha1, file.hashes.sha512
        )
    }

    suspend fun getModrinthProjectInfo(projectIdOrSlug: String): SimpleModInfo.Modrinth? {
        val project = ModrinthApi.project(projectIdOrSlug).escapeIfRight { return null }
        return getModrinthProjectInfo(project)
    }

    suspend fun getModrinthProjectInfo(project: ProjectJson): SimpleModInfo.Modrinth {
        val teamMembers = ModrinthApi.teamMembers(project.team)

        val authors = ModInfo.authorString(teamMembers.map { it.user.username })
        val projectUrl = "https://modrinth.com/mod/${project.slug}"

        return SimpleModInfo.Modrinth(
            project.title, authors, project.description, project.iconUrl?.let { IconSource.Url(it) }, projectUrl,
            project.id, project.slug, project.body
        )
    }

    suspend fun latestVersion(projectIdOrSlug: String, acceptableVersions: AcceptableVersions): VersionJson? {
        // Modrinth is guaranteed to always place the latest versions first for this endpoint. That is what I am counting on here.
        val versions = ModrinthApi.versions(acceptableVersions.toVersionsQuery(projectIdOrSlug))
        return versions.firstOrNull()
    }

    suspend fun chooseLatest(
        projectIdOrSlug: String, acceptableVersions: AcceptableVersions
    ): SimpleModFileInfo.Modrinth? {
        val project = ModrinthApi.project(projectIdOrSlug).escapeIfRight { return null }
        val version = latestVersion(projectIdOrSlug, acceptableVersions) ?: return null

        return getModrinthFileInfo(project, version)
    }

    suspend fun chooseLatest(
        projectIdOrSlugList: List<String>, acceptableVersions: AcceptableVersions,
        progress: ProgressListener = EMPTY_PROGRESS_LISTENER
    ): List<SimpleModFileInfo.Modrinth?> {
        // collect all the projects first so they get batched
        progress(0f, "Downloading projects' data...")
        val projects = coroutineScope {
            projectIdOrSlugList.map { idOrSlug ->
                async {
                    ModrinthApi.project(idOrSlug).escapeIfRight { return@async null }
                }
            }.awaitAll()
        }

        progress(0.5f, "Downloading versions' data...")

        // next, collect all the versions at the same time, so they too get batched
        val mods = coroutineScope {
            projects.map { project ->
                async projectAsync@{
                    if (project == null) return@projectAsync null

                    val version = project.versions.map { versionId ->
                        async versionAsync@{
                            ModrinthApi.version(versionId).escapeIfRight { return@versionAsync null }
                        }
                    }.awaitAll().asSequence().filterNotNull().filter { acceptableVersions.compatible(it) }
                        .sortedByDescending { it.datePublished }.firstOrNull() ?: return@projectAsync null

                    getModrinthFileInfo(project, version)
                }
            }.awaitAll()
        }

        progress(1f, "Latest files chosen.")

        return mods
    }

    fun getPrimaryFile(version: VersionJson): FileJson? {
        return version.files.firstOrNull { it.primary } ?: version.files.firstOrNull()
    }

    suspend fun getProjectAndVersion(
        dependency: DependencyJson, acceptableVersions: AcceptableVersions
    ): ProjectAndVersion? {
        return when {
            dependency.versionId != null -> {
                val version = ModrinthApi.version(dependency.versionId).escapeIfRight { return null }
                val project = ModrinthApi.project(version.projectId).escapeIfRight { return null }
                ProjectAndVersion(project, version)
            }
            dependency.projectId != null -> {
                val project = ModrinthApi.project(dependency.projectId).escapeIfRight { return null }
                val version = latestVersion(dependency.projectId, acceptableVersions) ?: return null
                ProjectAndVersion(project, version)
            }
            else -> null
        }
    }
}
