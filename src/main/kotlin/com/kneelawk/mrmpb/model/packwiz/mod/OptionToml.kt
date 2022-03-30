package com.kneelawk.mrmpb.model.packwiz.mod

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class OptionToml(val default: Boolean, val description: String, val optional: Boolean) : ToToml {
    companion object : FromToml<OptionToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): OptionToml {
            return OptionToml(
                toml.mustGetBoolean("default"),
                toml.mustGetString("description"),
                toml.mustGetBoolean("optional")
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return mapOf(
            "default" to default,
            "description" to description,
            "optional" to optional
        )
    }
}
