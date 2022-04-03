package com.kneelawk.mrmpb.model.modrinth.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatusJson {
    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,

    @SerialName("draft")
    DRAFT,

    @SerialName("unlisted")
    UNLISTED,

    @SerialName("archived")
    ARCHIVED,

    @SerialName("processing")
    PROCESSING,

    @SerialName("unknown")
    UNKNOWN;
}
