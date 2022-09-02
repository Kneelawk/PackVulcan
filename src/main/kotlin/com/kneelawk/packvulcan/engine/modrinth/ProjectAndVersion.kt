package com.kneelawk.packvulcan.engine.modrinth

import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.modrinth.project.ProjectJson
import com.kneelawk.packvulcan.model.modrinth.version.result.VersionJson

data class ProjectAndVersion(val project: ProjectJson, val version: VersionJson) {
    suspend fun toModInfo(): SimpleModInfo.Modrinth = ModrinthUtils.getModrinthInfo(project, version)
}
