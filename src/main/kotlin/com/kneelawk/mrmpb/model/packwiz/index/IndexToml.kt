package com.kneelawk.mrmpb.model.packwiz.index

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class IndexToml(val hashFormat: HashFormat, val files: List<FileToml>) : ToToml {
    companion object : FromToml<IndexToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): IndexToml {
            return IndexToml(
                HashFormat.fromString(toml.mustGetString("hash-format")),
                toml.mustGetTables("files").map { FileToml.fromToml(it) }
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["hash-format"] = hashFormat.toString()

        if (files.isNotEmpty()) {
            map["files"] = files.map { it.toToml() }
        }

        return map
    }
}