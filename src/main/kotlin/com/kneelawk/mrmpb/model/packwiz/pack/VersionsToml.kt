package com.kneelawk.mrmpb.model.packwiz.pack

import com.kneelawk.mrmpb.model.packwiz.FromToml
import com.kneelawk.mrmpb.model.packwiz.LoadError
import com.kneelawk.mrmpb.model.packwiz.ToToml
import com.kneelawk.mrmpb.model.packwiz.mustGetString
import com.moandjiezana.toml.Toml

data class VersionsToml(val minecraft: String, val loaderVersions: Map<String, String>) : ToToml {
    companion object : FromToml<VersionsToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): VersionsToml {
            val minecraft = toml.mustGetString("minecraft")
            val loaderVersions = mutableMapOf<String, String>()

            for (entry in toml.entrySet()) {
                if (entry.key != "minecraft" && entry.value is String) {
                    loaderVersions[entry.key] = entry.value as String
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
