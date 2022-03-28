package com.kneelawk.mrmpb.model.manifest.minecraft

import kotlinx.serialization.Serializable

@Serializable
data class LatestJson(
    val release: String,
    val snapshot: String
)