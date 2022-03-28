package com.kneelawk.mrmpb.model.manifest.minecraft

import kotlinx.serialization.Serializable

@Serializable
data class MinecraftManifestJson(
    val latest: LatestJson,
    val versions: List<VersionJson>
)

