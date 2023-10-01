package com.kneelawk.packvulcan.model.curseforge.project

import kotlinx.serialization.Serializable

@Serializable
data class LinksJson(
    val websiteUrl: String,
    val wikiUrl: String?,
    val issuesUrl: String?,
    val sourceUrl: String?,
)
