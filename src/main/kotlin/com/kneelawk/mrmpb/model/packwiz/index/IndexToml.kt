package com.kneelawk.mrmpb.model.packwiz.index

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class IndexToml(val hashFormat: HashFormat, val files: List<FileToml>) : ToToml {
    companion object : FromToml<IndexToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): IndexToml {
            return IndexToml(
                HashFormat.fromString(toml.mustGetString("hash-format")),
                // sometimes I've encountered index.toml files without a 'files' element
                toml.getTables("files")?.map { FileToml.fromToml(it) } ?: listOf()
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        // however, the 'files' element is listed as being required in the documentation, even if it's empty
        return mapOf(
            "hash-format" to hashFormat.toString(),
            "files" to files.map { it.toToml() }
        )
    }
}
