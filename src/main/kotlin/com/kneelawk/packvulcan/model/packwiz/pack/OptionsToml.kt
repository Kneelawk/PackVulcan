package com.kneelawk.packvulcan.model.packwiz.pack

import com.kneelawk.packvulcan.model.packwiz.FormatVersion
import com.kneelawk.packvulcan.model.packwiz.FromTomlVersioned
import com.kneelawk.packvulcan.model.packwiz.ToTomlVersioned
import com.moandjiezana.toml.Toml

private val overriddenKeys =
    setOf("meta-folder", "mods-folder", "acceptable-game-versions", "x-packvulcan-acceptable-loaders")

data class OptionsToml(
    val modsFolder: String?, val acceptableGameVersions: List<String>, val acceptableLoaders: List<String>,
    val options: Map<String, Any>
) : ToTomlVersioned {
    companion object : FromTomlVersioned<OptionsToml> {
        override fun fromToml(toml: Toml, packFormat: FormatVersion): OptionsToml {
            val modsFolder = toml.getString("meta-folder") ?: toml.getString("mods-folder")
            val acceptableGameVersions = toml.getList<String>("acceptable-game-versions", listOf())
            val acceptableLoaders = toml.getList<String>("x-packvulcan-acceptable-loaders", listOf())

            val options = mutableMapOf<String, Any>()
            for ((key, value) in toml.entrySet()) {
                if (!overriddenKeys.contains(key)) {
                    options[key] = value
                }
            }

            return OptionsToml(modsFolder, acceptableGameVersions, acceptableLoaders, options)
        }
    }

    override fun toToml(packFormat: FormatVersion): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map.putAll(options)
        modsFolder?.let { map[packFormat.metaFolderKey] = it }
        if (acceptableGameVersions.isNotEmpty()) map["acceptable-game-versions"] = acceptableGameVersions
        if (acceptableLoaders.isNotEmpty()) map["x-packvulcan-acceptable-loaders"] = acceptableLoaders
        return map
    }
}
