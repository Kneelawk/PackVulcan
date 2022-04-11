package com.kneelawk.packvulcan.model.modrinth.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryJson(
    val icon: String,
    val name: String,
    @SerialName("project_type")
    val projectType: String,
)
