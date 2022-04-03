package com.kneelawk.mrmpb.model.modrinth.version

import com.kneelawk.mrmpb.util.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class VersionJson(
    val name: String,
    @SerialName("version_number")
    val versionNumber: String,
    val changelog: String?,
    val dependencies: List<DependencyJson>?,
    @SerialName("game_versions")
    val gameVersions: List<String>,
    @SerialName("version_type")
    val versionType: VersionTypeJson,
    val loaders: List<String>,
    val featured: Boolean,
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("date_published")
    @Serializable(ZonedDateTimeSerializer::class)
    val datePublished: ZonedDateTime,
    val downloads: Long,
    @SerialName("changelog_url")
    @Deprecated("Changelogs have been moved to the `changelog` value.")
    val changelogUrl: String?,
    val files: List<FileJson>,
)
