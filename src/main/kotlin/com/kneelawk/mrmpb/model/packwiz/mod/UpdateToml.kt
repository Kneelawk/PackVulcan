package com.kneelawk.mrmpb.model.packwiz.mod

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml

data class UpdateToml(val curseforge: CurseforgeToml?, val modrinth: ModrinthToml?) : ToToml {
    companion object : FromToml<UpdateToml> {
        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): UpdateToml {
            return UpdateToml(
                toml.getTable("curseforge")?.let { CurseforgeToml.fromToml(it) },
                toml.getTable("modrinth")?.let { ModrinthToml.fromToml(it) }
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            curseforge?.toToml()?.from("curseforge"),
            modrinth?.toToml()?.from("modrinth")
        )
    }
}
