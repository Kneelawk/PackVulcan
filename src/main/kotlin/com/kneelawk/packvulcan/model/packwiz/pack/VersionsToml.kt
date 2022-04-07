package com.kneelawk.packvulcan.model.packwiz.pack

import com.kneelawk.packvulcan.model.packwiz.FromToml
import com.kneelawk.packvulcan.model.packwiz.LoadError
import com.kneelawk.packvulcan.model.packwiz.ToToml
import com.kneelawk.packvulcan.model.packwiz.mustGetString
import com.moandjiezana.toml.Toml

data class VersionsToml(val minecraft: String, val loaderVersions: Map<String, String>) : ToToml {
    companion object : FromToml<VersionsToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): VersionsToml {
            val minecraft = toml.mustGetString("minecraft")
            val loaderVersions = mutableMapOf<String, String>()

            for ((key, value) in toml.entrySet()) {
                if (key != "minecraft" && value is String) {
                    loaderVersions[key] = value
                }
            }

            return VersionsToml(minecraft, loaderVersions)
        }
    }

    override fun toToml(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["minecraft"] = minecraft
        map.putAll(loaderVersions)
        return map
    }
}
