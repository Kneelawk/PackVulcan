package com.kneelawk.mrmpb.model.packwiz.mod

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class ModToml(
    val name: String, val filename: String, val side: Side = Side.BOTH, val download: DownloadToml,
    val option: OptionToml?, val update: UpdateToml?
) : ToToml {
    companion object : FromToml<ModToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): ModToml {
            return ModToml(
                toml.mustGetString("name"),
                toml.mustGetString("filename"),
                toml.getString("side")?.let { Side.fromString(it) } ?: Side.BOTH,
                DownloadToml.fromToml(toml.mustGetTable("download")),
                toml.getTable("option")?.let { OptionToml.fromToml(it) },
                toml.getTable("update")?.let { UpdateToml.fromToml(it) }
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "name" to name,
            "filename" to filename,
            "side" to side.toString(),
            "download" to download.toToml(),
            option?.toToml()?.from("option"),
            update?.toToml()?.from("update")
        )
    }
}
