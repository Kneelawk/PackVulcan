package com.kneelawk.mrmpb.model.cache

import com.kneelawk.mrmpb.model.HashFormat
import com.kneelawk.mrmpb.util.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.EnumMap

@Serializable
data class ModFileCacheJson(
    @Serializable(ZonedDateTimeSerializer::class)
    val lastAccess: ZonedDateTime,
    val hashes: Map<HashFormat, String>,
)
