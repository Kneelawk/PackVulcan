package com.kneelawk.packvulcan.model.modrinth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SideCompatJson {
    @SerialName("required")
    REQUIRED,

    @SerialName("optional")
    OPTIONAL,

    @SerialName("unsupported")
    UNSUPPORTED,

    @SerialName("unknown")
    UNKNOWN;
}
