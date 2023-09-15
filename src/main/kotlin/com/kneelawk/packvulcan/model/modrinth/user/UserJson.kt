package com.kneelawk.packvulcan.model.modrinth.user

import com.kneelawk.packvulcan.util.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class UserJson(
    val username: String,
    val name: String?,
    val email: String?,
    val bio: String?,
    val id: String,
    @SerialName("github_id")
    val githubId: Int?,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @Serializable(ZonedDateTimeSerializer::class)
    val created: ZonedDateTime,
    val role: RoleJson
)
