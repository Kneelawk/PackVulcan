package com.kneelawk.packvulcan.model.modfile.quilt

import com.kneelawk.packvulcan.model.modfile.IconJson
import com.kneelawk.packvulcan.model.modfile.StringOrArrayJson
import kotlinx.serialization.Serializable

@Serializable
data class MetadataJson(
    val name: String? = null,
    val description: String? = null,
    val contributors: Map<String, StringOrArrayJson>? = null,
    val contact: Map<String, String>? = null,
    val license: LicenseListJson,
    val icon: IconJson? = null,
)
