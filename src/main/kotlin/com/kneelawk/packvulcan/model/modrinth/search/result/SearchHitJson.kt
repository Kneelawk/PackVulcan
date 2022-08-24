package com.kneelawk.packvulcan.model.modrinth.search.result

import com.kneelawk.packvulcan.model.modrinth.ProjectTypeJson
import com.kneelawk.packvulcan.model.modrinth.SideCompatJson
import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class SearchHitJson(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>?,
    @SerialName("display_categories")
    val displayCategories: List<String>?,
    @SerialName("client_side")
    val clientSide: SideCompatJson,
    @SerialName("server_side")
    val serverSide: SideCompatJson,
    @SerialName("project_type")
    val projectType: ProjectTypeJson,
    val downloads: Int,
    @SerialName("icon_url")
    val iconUrl: String?,
    @SerialName("project_id")
    val projectId: String,
    val author: String,
    val versions: List<String>,
    val follows: Int,
    @SerialName("date_created")
    @Serializable(ZonedDateTimeSerializer::class)
    val dateCreated: ZonedDateTime,
    @SerialName("date_modified")
    @Serializable(ZonedDateTimeSerializer::class)
    val dateModified: ZonedDateTime,
    @SerialName("latest_version")
    val latestVersion: String?,
    val license: String,
    val gallery: List<String>?
)
