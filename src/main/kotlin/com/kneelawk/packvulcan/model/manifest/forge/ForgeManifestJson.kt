package com.kneelawk.packvulcan.model.manifest.forge

import kotlinx.serialization.Serializable

@Serializable
data class ForgeManifestJson(val gameVersions: List<GameVersionJson>)
