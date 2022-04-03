package com.kneelawk.mrmpb.model.modrinth.team

import com.kneelawk.mrmpb.model.modrinth.user.UserJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamMemberJson(
    @SerialName("team_id")
    val teamId: String,
    val user: UserJson,
    val role: String,
    val permissions: Int?,
    val accepted: Boolean,
)
