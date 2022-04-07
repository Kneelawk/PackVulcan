package com.kneelawk.packvulcan.model.modfile.quilt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LoadTypeJson {
    @SerialName("always")
    ALWAYS,

    @SerialName("if_possible")
    IF_POSSIBLE,

    @SerialName("if_required")
    IF_REQUIRED;
}
