package com.kneelawk.mrmpb.model.packwiz.pack

import com.kneelawk.mrmpb.model.packwiz.FromToml
import com.kneelawk.mrmpb.model.packwiz.ToToml
import com.moandjiezana.toml.Toml

data class OptionsToml(val modsFolder: String?, val options: Map<String, String>) : ToToml {
    companion object : FromToml<OptionsToml> {
        override fun fromToml(toml: Toml): OptionsToml {
            val modsFolder = toml.getString("mods-folder")
            val options = mutableMapOf<String, String>()
            for ((key, value) in toml.entrySet()) {
                if (key != "mods-folder" && value is String) {
                    options[key] = value
                }
            }

            return OptionsToml(modsFolder, options)
        }
    }

    override fun toToml(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map.putAll(options)
        modsFolder?.let { map["mods-folder"] = it }
        return map
    }
}
