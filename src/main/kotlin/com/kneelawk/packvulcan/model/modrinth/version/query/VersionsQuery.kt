package com.kneelawk.packvulcan.model.modrinth.version.query

data class VersionsQuery(
    val projectIdOrSlug: String, val loaders: Set<String> = setOf(), val gameVersions: Set<String> = setOf(),
    val featured: Boolean? = null
)
