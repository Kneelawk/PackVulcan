package com.kneelawk.packvulcan.model.modrinth.project

import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class ProjectJson(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @SerialName("client_side")
    val clientSide: SideCompatJson,
    @SerialName("server_side")
    val serverSide: SideCompatJson,
    val body: String,
    @SerialName("issues_url")
    val issuesUrl: String?,
    @SerialName("source_url")
    val sourceUrl: String?,
    @SerialName("wiki_url")
    val wikiUrl: String?,
    @SerialName("discord_url")
    val discordUrl: String?,
    @SerialName("donation_urls")
    val donationUrls: List<DonationUrlJson>?,
    @SerialName("project_type")
    val projectType: ProjectTypeJson,
    val downloads: Long,
    @SerialName("icon_url")
    val iconUrl: String?,
    val id: String,
    val team: String,
    @Deprecated("Project bodies have been moved to the `body` value.")
    @SerialName("body_url")
    val bodyUrl: String?,
    @SerialName("moderator_message")
    val moderatorMessage: ModeratorMessageJson?,
    @Serializable(ZonedDateTimeSerializer::class)
    val published: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class)
    val updated: ZonedDateTime,
    val followers: Long,
    val status: StatusJson,
    val license: LicenseJson,
    val versions: List<String>,
    val gallery: List<GalleryJson>?,
)
