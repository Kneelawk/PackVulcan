package com.kneelawk.packvulcan.model.modrinth.version.result

import kotlinx.serialization.Serializable

@Serializable
data class FileJson(
    val hashes: HashesJson,
    val url: String,
    val filename: String,
    val primary: Boolean,
    val size: Int,
)
