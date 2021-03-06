package com.kneelawk.packvulcan.model.modrinth.project

import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class GalleryJson(
    val url: String,
    val featured: Boolean,
    val title: String?,
    val description: String?,
    @Serializable(ZonedDateTimeSerializer::class)
    val created: ZonedDateTime,
)
