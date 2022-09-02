package com.kneelawk.packvulcan.model.modrinth.version.query

import com.kneelawk.packvulcan.model.HashFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateByHash(
    val hashes: List<String>, val algorithm: HashFormat, val loaders: List<String>,
    @SerialName("game_versions") val gameVersions: List<String>
)
