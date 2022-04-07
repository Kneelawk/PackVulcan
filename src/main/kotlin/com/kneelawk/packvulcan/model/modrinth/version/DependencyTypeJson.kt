package com.kneelawk.packvulcan.model.modrinth.version

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DependencyTypeJson {
    @SerialName("required")
    REQUIRED,

    @SerialName("optional")
    OPTIONAL,

    @SerialName("incompatible")
    INCOMPATIBLE;
}
