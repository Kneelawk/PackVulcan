package com.kneelawk.packvulcan.model.curseforge.project

import kotlinx.serialization.Serializable

@Serializable
data class LogoJson(
    val id: Long,
    val modId: Long,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val url: String,
)
