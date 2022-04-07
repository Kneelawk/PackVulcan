package com.kneelawk.packvulcan.model.packwiz.mod

import com.kneelawk.packvulcan.model.packwiz.FromToml
import com.kneelawk.packvulcan.model.packwiz.LoadError
import com.kneelawk.packvulcan.model.packwiz.ToToml
import com.kneelawk.packvulcan.model.packwiz.mustGetString
import com.moandjiezana.toml.Toml

data class ModrinthToml(val modId: String, val version: String) : ToToml {
    companion object : FromToml<ModrinthToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): ModrinthToml {
            return ModrinthToml(
                toml.mustGetString("mod-id"),
                toml.mustGetString("version")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return mapOf(
            "mod-id" to modId,
            "version" to version
        )
    }
}
