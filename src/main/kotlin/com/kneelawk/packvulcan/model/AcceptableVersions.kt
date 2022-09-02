package com.kneelawk.packvulcan.model

import com.kneelawk.packvulcan.model.modrinth.version.query.VersionsQuery
import com.kneelawk.packvulcan.model.modrinth.version.result.VersionJson

data class AcceptableVersions(val minecraft: Set<String> = setOf(), val loaders: Set<String> = setOf()) {
    fun toVersionsQuery(projectIdOrSlug: String, featured: Boolean? = null): VersionsQuery =
        VersionsQuery(projectIdOrSlug, loaders, minecraft, featured)

    fun loadersCompatible(other: Sequence<String>) = other.any { loaders.contains(it) }

    fun minecraftCompatible(other: Sequence<String>) = other.any { minecraft.contains(it) }

    fun loadersCompatible(other: List<String>) = other.any { loaders.contains(it) }

    fun minecraftCompatible(other: List<String>) = other.any { minecraft.contains(it) }

    fun compatible(version: VersionJson) =
        loadersCompatible(version.loaders) && minecraftCompatible(version.gameVersions)
}
