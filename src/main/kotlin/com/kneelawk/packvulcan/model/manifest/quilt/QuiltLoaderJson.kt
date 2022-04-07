package com.kneelawk.packvulcan.model.manifest.quilt

import kotlinx.serialization.Serializable

@Serializable
data class QuiltLoaderJson(
    val separator: String,
    val build: Int,
    val maven: String,
    val version: String
)
