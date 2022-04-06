package com.kneelawk.mrmpb.model.modfile.quilt

import kotlinx.serialization.Serializable

@Serializable
data class ProvidesJson(
    val id: String,
    val version: String? = null,
)
