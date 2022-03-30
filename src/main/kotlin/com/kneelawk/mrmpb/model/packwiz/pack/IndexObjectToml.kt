package com.kneelawk.mrmpb.model.packwiz.pack

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class IndexObjectToml(val file: String, val hashFormat: HashFormat, val hash: String) : ToToml {
    companion object : FromToml<IndexObjectToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): IndexObjectToml {
            return IndexObjectToml(
                toml.mustGetString("file"),
                HashFormat.fromString(toml.mustGetString("hash-format")),
                toml.mustGetString("hash")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return mapOf(
            "file" to file,
            "hash-format" to hashFormat.toString(),
            "hash" to hash
        )
    }
}
