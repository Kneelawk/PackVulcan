package com.kneelawk.mrmpb.model.packwiz.pack

import com.kneelawk.mrmpb.model.packwiz.*
import com.moandjiezana.toml.Toml
import mu.KotlinLogging

data class PackToml(
    val name: String, val author: String?, val version: String?, val description: String?, val packFormat: String?,
    val index: IndexObjectToml,
    val versions: VersionsToml
) : ToToml {
    companion object : FromToml<PackToml> {
        const val DEFAULT_PACK_FORMAT = "packwiz:1.0.0"

        private val log = KotlinLogging.logger { }

        @Throws(LoadError::class)
        override fun fromToml(toml: Toml): PackToml {
            val packFormat: String? = toml.getString("pack-format")

            if (packFormat != null && packFormat != DEFAULT_PACK_FORMAT) {
                log.warn("Unknown pack format '$packFormat'")
            }

            return PackToml(
                toml.mustGetString("name"),
                toml.getString("author"),
                toml.getString("version"),
                toml.getString("description"),
                packFormat,
                IndexObjectToml.fromToml(toml.mustGetTable("index")),
                VersionsToml.fromToml(toml.mustGetTable("versions"))
            )
        }
    }

    override fun toToml(): Map<String, Any> {
        return maybeMapOf(
            "name" to name,
            author?.from("author"),
            version?.from("version"),
            description?.from("description"),
            packFormat?.from("pack-format"),
            "index" to index.toToml(),
            "versions" to versions.toToml()
        )
    }
}
