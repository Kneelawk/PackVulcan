package com.kneelawk.mrmpb.model.modfile.quilt

import kotlinx.serialization.Serializable

@Serializable
data class MinecraftJson(
    val environment: EnvironmentJson? = null
)
