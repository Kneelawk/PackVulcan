package com.kneelawk.packvulcan.model.modrinth.project

import kotlinx.serialization.Serializable

@Serializable
data class LicenseJson(val id: String, val name: String, val url: String?)
