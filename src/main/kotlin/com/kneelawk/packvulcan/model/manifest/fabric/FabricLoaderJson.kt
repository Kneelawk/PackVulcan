package com.kneelawk.packvulcan.model.manifest.fabric

import kotlinx.serialization.Serializable

@Serializable
data class FabricLoaderJson(
    val separator: String,
    val build: Int,
    val maven: String,
    val version: String,
    val stable: Boolean
)
