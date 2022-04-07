package com.kneelawk.packvulcan.model.modfile.fabric

import com.kneelawk.packvulcan.model.modfile.IconJson
import kotlinx.serialization.Serializable

@Serializable
data class FabricModJson(
    val schemaVersion: Int,
    val id: String,
    val version: String,
    val environment: EnvironmentListJson? = null,
    val name: String? = null,
    val description: String? = null,
    val authors: List<PersonJson>? = null,
    val contributors: List<PersonJson>? = null,
    val contact: Map<String, String>? = null,
    val icon: IconJson? = null
)
