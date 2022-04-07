package com.kneelawk.packvulcan.model.modrinth.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RoleJson {
    @SerialName("admin")
    ADMIN,

    @SerialName("moderator")
    MODERATOR,

    @SerialName("developer")
    DEVELOPER;
}
