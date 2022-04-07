package com.kneelawk.packvulcan.model.modrinth.version

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DependencyJson(
    @SerialName("version_id")
    val versionId: String?,
    @SerialName("project_id")
    val projectId: String?,
    @SerialName("dependency_type")
    val dependencyType: DependencyTypeJson
)
