package com.kneelawk.packvulcan.model.curseforge.file

import kotlinx.serialization.Serializable

@Serializable
data class HashJson(val value: String, val algo: HashAlgoJson)
