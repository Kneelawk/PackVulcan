package com.kneelawk.packvulcan.model.curseforge.project

import kotlinx.serialization.Serializable

@Serializable
data class AuthorJson(val id: Long, val name: String, val url: String)
