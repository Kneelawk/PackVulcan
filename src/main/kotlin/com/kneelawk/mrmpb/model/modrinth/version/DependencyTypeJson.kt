package com.kneelawk.mrmpb.model.modrinth.version

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
