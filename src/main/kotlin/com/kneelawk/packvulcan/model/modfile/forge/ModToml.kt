package com.kneelawk.packvulcan.model.modfile.forge

import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml

data class ModToml(
    val modId: String,
    val namespace: String?,
    val version: String = "1",
    val displayName: String?,
    val description: String?,
    val logoFile: String?,
    val logoBlur: Boolean = true,
    val updateJSONURL: String?,
    val modProperties: Map<String, Any>?,
    val credits: String?,
    val authors: String?,
    val displayURL: String?,
) : ToToml {
    companion object : FromToml<ModToml> {
        override fun fromToml(toml: Toml): ModToml {
            return ModToml(
                toml.mustGetString("modId"),
                toml.getString("namespace"),
                toml.getString("version", "1"),
                toml.getString("displayName"),
                toml.getString("description"),
                toml.getString("logoFile"),
                toml.getBoolean("logoBlur", true),
                toml.getString("updateJSONURL"),
                toml.getTable("modproperties")?.toMap(),
                toml.getString("credits"),
                toml.getString("authors"),
                toml.getString("displayURL"),
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "modId" to modId,
            namespace?.from("namespace"),
            "version" to version,
            displayName?.from("displayName"),
            description?.from("description"),
            logoFile?.from("logoFile"),
            "logoBlur" to logoBlur,
            updateJSONURL?.from("updateJSONURL"),
            modProperties?.from("modproperties"), // Note, the first `p` in properties should be lowercase
            credits?.from("credits"),
            authors?.from("authors"),
            displayURL?.from("displayURL")
        )
    }
}
