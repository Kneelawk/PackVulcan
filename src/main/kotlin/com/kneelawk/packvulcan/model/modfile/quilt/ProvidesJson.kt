package com.kneelawk.packvulcan.model.modfile.quilt

import kotlinx.serialization.Serializable

@Serializable
data class ProvidesJson(
    val id: String,
    val version: String? = null,
)
