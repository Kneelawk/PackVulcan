package com.kneelawk.mrmpb.model.packwiz.mod

import com.kneelawk.mrmpb.model.HashFormat
import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class DownloadToml(val hashFormat: HashFormat, val hash: String, val url: String) : ToToml {
    companion object : FromToml<DownloadToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): DownloadToml {
            return DownloadToml(
                HashFormat.fromString(toml.mustGetString("hash-format")),
                toml.mustGetString("hash"),
                toml.mustGetString("url")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return mapOf(
            "hash-format" to hashFormat.toString(),
            "hash" to hash,
            "url" to url
        )
    }
}
