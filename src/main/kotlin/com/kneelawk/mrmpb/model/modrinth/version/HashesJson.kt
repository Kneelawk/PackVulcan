package com.kneelawk.mrmpb.model.modrinth.version

import kotlinx.serialization.Serializable

@Serializable
data class HashesJson(val sha512: String, val sha1: String)
