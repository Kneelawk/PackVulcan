package com.kneelawk.mrmpb.model.modrinth.project

import kotlinx.serialization.Serializable

@Serializable
data class ModeratorMessageJson(val message: String, val body: String?)
