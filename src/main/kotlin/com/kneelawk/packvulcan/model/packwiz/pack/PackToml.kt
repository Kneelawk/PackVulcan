package com.kneelawk.packvulcan.model.packwiz.pack

import com.kneelawk.packvulcan.model.packwiz.*
import com.moandjiezana.toml.Toml
import mu.KotlinLogging

data class PackToml(
    val name: String, val author: String?, val version: String?, val description: String?,
    val packFormat: FormatVersion, val index: IndexObjectToml, val versions: VersionsToml, val options: OptionsToml?
) : ToToml {
    companion object : FromToml<PackToml> {
        private val log = KotlinLogging.logger { }

        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): PackToml {
            val packFormat = FormatVersion.fromString(toml.getString("pack-format"))

            if (packFormat.isUnknown) {
                log.warn("Unknown pack format '$packFormat'")
            }

            return PackToml(
                toml.mustGetString("name"),
                toml.getString("author"),
                toml.getString("version"),
                toml.getString("description"),
                packFormat,
                IndexObjectToml.fromToml(toml.mustGetTable("index")),
                VersionsToml.fromToml(toml.mustGetTable("versions")),
                toml.getTable("options")?.let { OptionsToml.fromToml(it, packFormat) }
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "name" to name,
            author?.from("author"),
            version?.from("version"),
            description?.from("description"),
            "pack-format" to packFormat.toString(),
            "index" to index.toToml(),
            "versions" to versions.toToml(),
            options?.toToml(packFormat)?.from("options")
        )
    }
}
