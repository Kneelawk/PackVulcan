package com.kneelawk.mrmpb.model.modrinth.project

import kotlinx.serialization.Serializable

@Serializable
data class DonationUrlJson(
    val id: String,
    val platform: String,
    val url: String,
)
