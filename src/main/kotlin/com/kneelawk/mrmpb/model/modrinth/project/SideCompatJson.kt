package com.kneelawk.mrmpb.model.modrinth.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SideCompatJson {
    @SerialName("required")
    REQUIRED,

    @SerialName("optional")
    OPTIONAL,

    @SerialName("unsupported")
    UNSUPPORTED;
}