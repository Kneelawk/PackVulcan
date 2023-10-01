package com.kneelawk.packvulcan.model.curseforge.file

import com.kneelawk.packvulcan.util.EnumIntSerializer
import com.kneelawk.packvulcan.util.SerialInt
import kotlinx.serialization.Serializable

@Serializable(HashAlgoSerializer::class)
enum class HashAlgoJson {
    @SerialInt(1)
    SHA1,

    @SerialInt(2)
    MD5
}

object HashAlgoSerializer :
    EnumIntSerializer<HashAlgoJson>("com.kneelawk.packvulcan.model.curseforge.file.HashAlgoJson", HashAlgoJson::class)
