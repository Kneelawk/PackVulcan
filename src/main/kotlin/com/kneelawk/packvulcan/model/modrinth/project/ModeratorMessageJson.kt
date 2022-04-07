package com.kneelawk.packvulcan.model.modrinth.project

import kotlinx.serialization.Serializable

@Serializable
data class ModeratorMessageJson(val message: String, val body: String?)
