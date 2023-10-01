package com.kneelawk.packvulcan.model.curseforge.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectJson(
    val id: Long,
    val gameId: Int,
    val name: String,
    val slug: String,
    val links: LinksJson,
    val summary: String,
    val authors: List<AuthorJson>,
    val logo: LogoJson,
)
