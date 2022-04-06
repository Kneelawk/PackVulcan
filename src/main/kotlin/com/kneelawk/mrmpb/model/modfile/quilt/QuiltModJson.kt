package com.kneelawk.mrmpb.model.modfile.quilt

import com.kneelawk.mrmpb.model.modfile.StringOrArrayJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModJson(
    @SerialName("schema_version")
    val schemaVersion: Int,
    @SerialName("quilt_loader")
    val quiltLoader: QuiltLoaderJson,
    val mixin: StringOrArrayJson? = null,
    @SerialName("access_widener")
    val accessWidener: StringOrArrayJson? = null,
    val minecraft: MinecraftJson? = null,
)
