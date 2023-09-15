package com.kneelawk.packvulcan.model.packwiz.pack

import com.kneelawk.packvulcan.model.HashFormat
import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class IndexObjectToml(val file: String, val hashFormat: HashFormat, val hash: String?) : ToToml {
    companion object : FromToml<IndexObjectToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): IndexObjectToml {
            return IndexObjectToml(
                toml.mustGetString("file"),
                HashFormat.fromString(toml.mustGetString("hash-format")),
                toml.getString("hash")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "file" to file,
            "hash-format" to hashFormat.toString(),
            hash?.from("hash")
        )
    }
}
