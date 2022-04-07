package com.kneelawk.packvulcan.model.modrinth.version

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VersionTypeJson {
    @SerialName("release")
    RELEASE,

    @SerialName("beta")
    BETA,

    @SerialName("alpha")
    ALPHA;
}
