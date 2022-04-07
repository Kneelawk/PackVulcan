package com.kneelawk.packvulcan.model.modfile.forge

import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class DependencyToml(
    val modId: String,
    val mandatory: Boolean,
    val versionRange: String?,
    val ordering: String?,
    val side: String?
) : ToToml {
    companion object : FromToml<DependencyToml> {
        override fun fromToml(toml: Toml): DependencyToml {
            return DependencyToml(
                toml.mustGetString("modId"),
                toml.mustGetBoolean("mandatory"),
                toml.getString("versionRange"),
                toml.getString("ordering"),
                toml.getString("side")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "modId" to modId,
            "mandatory" to mandatory,
            versionRange?.from("versionRange"),
            ordering?.from("ordering"),
            side?.from("side")
        )
    }
}
