package com.kneelawk.packvulcan.model.modrinth.version.query

import com.kneelawk.packvulcan.model.HashFormat
import kotlinx.serialization.Serializable

@Serializable
data class HashLookup(val hashes: List<String>, val algorithm: HashFormat)
