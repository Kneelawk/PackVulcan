package com.kneelawk.packvulcan.model.packwiz

sealed class FormatVersion {
    open val isUnknown: Boolean = false

    open val metafileExtension = ".pw.toml"
    open val metafilesOutsideModsDir = true

    open val metaFolderKey = "meta-folder"

    object Packwiz_1_1_0 : FormatVersion() {
        override fun toString(): String = "packwiz:1.1.0"
    }

    object Packwiz_1_0_0 : FormatVersion() {
        override val metafileExtension = ".toml"
        override val metafilesOutsideModsDir = false
        override val metaFolderKey = "mods-folder"

        override fun toString(): String = "packwiz:1.0.0"
    }

    data class Unknown(val packFormat: String) : FormatVersion() {
        override val isUnknown = true
        override fun toString(): String = packFormat
    }

    companion object {
        val NEW_PACK_FORMAT = Packwiz_1_1_0

        fun fromString(packFormat: String?): FormatVersion {
            return when (packFormat) {
                null -> Packwiz_1_0_0
                "packwiz:1.0.0" -> Packwiz_1_0_0
                "packwiz:1.1.0" -> Packwiz_1_1_0
                else -> Unknown(packFormat)
            }
        }
    }
}
