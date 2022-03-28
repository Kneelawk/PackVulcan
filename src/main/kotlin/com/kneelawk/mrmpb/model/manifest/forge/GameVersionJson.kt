package com.kneelawk.mrmpb.model.manifest.forge

import kotlinx.serialization.Serializable

@Serializable
data class GameVersionJson(val id: String, val loaders: List<LoaderJson>)
