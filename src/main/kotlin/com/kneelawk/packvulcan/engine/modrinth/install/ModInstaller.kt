package com.kneelawk.packvulcan.engine.modrinth.install

import com.kneelawk.packvulcan.engine.modrinth.DependencyCollector
import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import com.kneelawk.packvulcan.engine.modrinth.ProjectAndVersion
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.SimpleModFileInfo
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi

object ModInstaller {
    suspend fun doInstall(
        request: InstallRequest, acceptableVersions: AcceptableVersions, installedProjects: Set<String>,
        progressMsg: (String) -> Unit = {}, autoInstall: Boolean = true,
    ): Result {
        progressMsg("Getting Mod...")

        val project = ModrinthApi.project(request.projectId).escapeIfRight {
            return ProjectError(request.projectId)
        }

        val version = when (request.version) {
            InstallVersion.Latest -> ModrinthUtils.latestVersion(request.projectId, acceptableVersions)
            is InstallVersion.Specific -> ModrinthApi.version(request.version.versionId).leftOrNull()
        } ?: run {
            return VersionError(project, request.version)
        }

        val pav = ProjectAndVersion(project, version)

        progressMsg("Getting Deps...")

        val deps = DependencyCollector.collectDependencies(version.dependencies, acceptableVersions, installedProjects)

        return if (autoInstall && deps.isEmpty()) {
            InstallSingle(pav.toModInfo())
        } else {
            Dependencies(pav.toModInfo(), deps)
        }
    }

    sealed interface Result
    data class InstallSingle(val mod: SimpleModFileInfo) : Result
    data class Dependencies(val mod: SimpleModFileInfo, val dependencies: List<SimpleModFileInfo>) : Result
    data class ProjectError(val projectId: String) : Result
    data class VersionError(val project: ProjectJson, val installVersion: InstallVersion) : Result
}
