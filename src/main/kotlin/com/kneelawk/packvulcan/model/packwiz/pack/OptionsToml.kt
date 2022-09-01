package com.kneelawk.packvulcan.model.packwiz.pack

import com.kneelawk.packvulcan.model.packwiz.FormatVersion
import com.kneelawk.packvulcan.model.packwiz.FromTomlVersioned
import com.kneelawk.packvulcan.model.packwiz.ToTomlVersioned
import com.moandjiezana.toml.Toml

private val overriddenKeys =
    setOf("meta-folder", "mods-folder", "acceptable-game-versions", "x-packvulcan-acceptable-loaders")

data class OptionsToml(
    val metaFolder: String? = null, val metaFolderBase: String? = null,
    val acceptableGameVersions: List<String> = listOf(), val acceptableLoaders: List<String> = listOf(),
    val options: Map<String, Any> = mapOf()
) : ToTomlVersioned {
    companion object : FromTomlVersioned<OptionsToml> {
        override fun fromToml(toml: Toml, packFormat: FormatVersion): OptionsToml {
            val metaFolder = toml.getString("meta-folder") ?: toml.getString("mods-folder")
            val metaFolderBase = toml.getString("meta-folder-base")
            val acceptableGameVersions = toml.getList<String>("acceptable-game-versions", listOf())
            val acceptableLoaders = toml.getList<String>("x-packvulcan-acceptable-loaders", listOf())

            val options = mutableMapOf<String, Any>()
            for ((key, value) in toml.entrySet()) {
                if (!overriddenKeys.contains(key)) {
                    options[key] = value
                }
            }

            return OptionsToml(metaFolder, metaFolderBase, acceptableGameVersions, acceptableLoaders, options)
        }
    }

    override fun toToml(packFormat: FormatVersion): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map.putAll(options)
        metaFolder?.let { map[packFormat.metaFolderKey] = it }
        metaFolderBase?.let { map["meta-folder-base"] = it }
        if (acceptableGameVersions.isNotEmpty()) map["acceptable-game-versions"] = acceptableGameVersions
        if (acceptableLoaders.isNotEmpty()) map["x-packvulcan-acceptable-loaders"] = acceptableLoaders
        return map
    }
}
