package com.kneelawk.packvulcan.model.modrinth.version.result

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DependencyJson(
    @SerialName("version_id")
    val versionId: String?,
    @SerialName("project_id")
    val projectId: String?,
    @SerialName("file_name")
    val filename: String?,
    @SerialName("dependency_type")
    val dependencyType: DependencyTypeJson
)
