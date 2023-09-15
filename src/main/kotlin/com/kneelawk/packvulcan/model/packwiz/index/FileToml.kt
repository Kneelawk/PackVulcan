package com.kneelawk.packvulcan.model.packwiz.index

import com.kneelawk.packvulcan.model.HashFormat
import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class FileToml(
    val file: String, val hash: String?, val alias: String?, val hashFormat: HashFormat?, val metafile: Boolean = false,
    val preserve: Boolean = false
) : ToToml {
    companion object : FromToml<FileToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): FileToml {
            return FileToml(
                toml.mustGetString("file"),
                toml.getString("hash"),
                toml.getString("alias"),
                toml.getString("hash-format")?.let { formatStr -> HashFormat.fromString(formatStr) },
                toml.getBoolean("metafile", false),
                toml.getBoolean("preserve", false)
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["file"] = file
        hash?.let { map["hash"] = it }
        alias?.let { map["alias"] = it }
        hashFormat?.let { map["hash-format"] = it }
        if (metafile) {
            map["metafile"] = true
        }
        if (preserve) {
            map["preserve"] = true
        }
        return map
    }
}
