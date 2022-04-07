package com.kneelawk.packvulcan.model.modfile.quilt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EnvironmentJson {
    @SerialName("*")
    ALL,

    @SerialName("client")
    CLIENT,

    @SerialName("dedicated_server")
    DEDICATED_SERVER;
}
