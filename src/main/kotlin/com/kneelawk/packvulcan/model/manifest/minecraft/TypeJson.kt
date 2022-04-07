package com.kneelawk.packvulcan.model.manifest.minecraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TypeJson {
    @SerialName("old_alpha")
    OLD_ALPHA,

    @SerialName("old_beta")
    OLD_BETA,

    @SerialName("release")
    RELEASE,

    @SerialName("snapshot")
    SNAPSHOT;
}