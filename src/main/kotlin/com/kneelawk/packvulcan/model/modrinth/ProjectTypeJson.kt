package com.kneelawk.packvulcan.model.modrinth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTypeJson {
    @SerialName("mod")
    MOD,

    @SerialName("modpack")
    MODPACK;
}
