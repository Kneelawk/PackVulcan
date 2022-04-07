package com.kneelawk.packvulcan.model.cache

import com.kneelawk.packvulcan.model.HashFormat
import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class ModFileCacheJson(
    @Serializable(ZonedDateTimeSerializer::class)
    val lastAccess: ZonedDateTime,
    val hashes: Map<HashFormat, String>,
)
