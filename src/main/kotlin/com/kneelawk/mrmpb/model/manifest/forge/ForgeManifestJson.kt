package com.kneelawk.mrmpb.model.manifest.forge

import kotlinx.serialization.Serializable

@Serializable
data class ForgeManifestJson(val gameVersions: List<GameVersionJson>)
