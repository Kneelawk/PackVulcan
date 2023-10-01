package com.kneelawk.packvulcan.model.curseforge.file

import kotlinx.serialization.Serializable

@Serializable
data class FileJson(
    val id: Long,
    val gameId: Int,
    val modId: Long,
    val displayName: String,
    val fileName: String,
    val hashes: List<HashJson>,
    val gameVersions: List<String>,
)
