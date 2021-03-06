package com.kneelawk.packvulcan.model.packwiz.mod

import com.kneelawk.packvulcan.model.packwiz.FromToml
import com.kneelawk.packvulcan.model.packwiz.LoadError
import com.kneelawk.packvulcan.model.packwiz.ToToml
import com.kneelawk.packvulcan.model.packwiz.mustGetLong
import com.moandjiezana.toml.Toml

data class CurseforgeToml(val fileId: Long, val projectId: Long) : ToToml {
    companion object : FromToml<CurseforgeToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): CurseforgeToml {
            return CurseforgeToml(
                toml.mustGetLong("file-id"),
                toml.mustGetLong("project-id")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return mapOf(
            "file-id" to fileId,
            "project-id" to projectId
        )
    }
}
