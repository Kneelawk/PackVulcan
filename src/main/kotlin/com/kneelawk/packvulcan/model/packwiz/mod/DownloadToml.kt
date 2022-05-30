package com.kneelawk.packvulcan.model.packwiz.mod

import com.kneelawk.packvulcan.model.HashFormat
import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class DownloadToml(val url: String?, val hashFormat: HashFormat, val hash: String, val mode: String) : ToToml {
    companion object : FromToml<DownloadToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): DownloadToml {
            return DownloadToml(
                toml.getString("url"),
                HashFormat.fromString(toml.mustGetString("hash-format")),
                toml.mustGetString("hash"),
                toml.getString("mode") ?: ""
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            url?.from("url"),
            "hash-format" to hashFormat.toString(),
            "hash" to hash,
            "mode" to mode
        )
    }
}
