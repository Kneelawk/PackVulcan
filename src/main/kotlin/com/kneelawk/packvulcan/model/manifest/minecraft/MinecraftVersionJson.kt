package com.kneelawk.packvulcan.model.manifest.minecraft

import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class MinecraftVersionJson(
    val version: String,
    @SerialName("version_type")
    val versionType: VersionTypeJson,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val date: ZonedDateTime,
    val major: Boolean,
)
