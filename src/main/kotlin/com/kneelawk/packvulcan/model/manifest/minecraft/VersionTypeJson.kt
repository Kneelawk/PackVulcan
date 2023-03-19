package com.kneelawk.packvulcan.model.manifest.minecraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VersionTypeJson {
    @SerialName("alpha")
    ALPHA,

    @SerialName("beta")
    BETA,

    @SerialName("release")
    RELEASE,

    @SerialName("snapshot")
    SNAPSHOT;
}
