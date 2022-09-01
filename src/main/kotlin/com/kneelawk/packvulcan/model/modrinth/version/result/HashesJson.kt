package com.kneelawk.packvulcan.model.modrinth.version.result

import kotlinx.serialization.Serializable

@Serializable
data class HashesJson(val sha512: String, val sha1: String)
