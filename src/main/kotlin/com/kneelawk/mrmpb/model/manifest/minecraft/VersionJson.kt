package com.kneelawk.mrmpb.model.manifest.minecraft

import com.kneelawk.mrmpb.util.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class VersionJson(
    val id: String,
    val type: TypeJson,
    val url: String,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val time: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val releaseTime: ZonedDateTime,
    val sha1: String,
    val complianceLevel: Int
)