package com.kneelawk.packvulcan.model.modfile.forge

import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class ModsToml(
    val modLoader: String,
    val loaderVersion: String,
    val license: String,
    val showAsResourcePack: Boolean = false,
    val properties: Map<String, Any>?,
    val issueTrackerURL: String?,
    val mods: List<ModToml>,
    val dependencies: Map<String, List<DependencyToml>>?,
) : ToToml {
    companion object : FromToml<ModsToml> {
        override fun fromToml(toml: Toml): ModsToml {
            return ModsToml(
                toml.mustGetString("modLoader"),
                toml.mustGetString("loaderVersion"),
                toml.mustGetString("license"),
                toml.getBoolean("showAsResourcePack", false),
                toml.getTable("properties")?.toMap(),
                toml.getString("issueTrackerURL"),
                toml.mustGetTables("mods").map { ModToml.fromToml(it) },
                toml.getTable("dependencies")?.let { dependencies ->
                    val newMap = mutableMapOf<String, List<DependencyToml>>()
                    for ((key, _) in dependencies.entrySet()) {
                        newMap[key] = dependencies.getTables(key).map { DependencyToml.fromToml(it) }
                    }
                    newMap
                }
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "modLoader" to modLoader,
            "loaderVersion" to loaderVersion,
            "license" to license,
            "showAsResourcePack" to showAsResourcePack,
            properties?.from("properties"),
            issueTrackerURL?.from("issueTrackerURL"),
            "mods" to mods.map { it.toToml() },
            dependencies?.mapValues { entry -> entry.value.map { it.toToml() } }?.from("dependencies")
        )
    }
}
