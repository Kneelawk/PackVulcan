package com.kneelawk.mrmpb.model.modrinth.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTypeJson {
    @SerialName("mod")
    MOD,

    @SerialName("modpack")
    MODPACK;
}