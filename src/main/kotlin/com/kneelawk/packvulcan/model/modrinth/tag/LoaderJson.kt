package com.kneelawk.packvulcan.model.modrinth.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoaderJson(
    val icon: String,
    val name: String,
    @SerialName("supported_project_types")
    val supportedProjectTypes: List<String>
)
