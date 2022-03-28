package com.kneelawk.mrmpb.model.manifest.forge

import kotlinx.serialization.Serializable

@Serializable
data class LoaderJson(val id: String, val url: String, val stable: Boolean)
