package com.kneelawk.packvulcan.model.packwiz

import com.kneelawk.packvulcan.model.modrinth.SideCompatJson

enum class Side {
    BOTH,
    CLIENT,
    SERVER;

    companion object {
        @Throws(LoadError::class)
        fun fromString(string: String): Side {
            return when (string.lowercase()) {
                "both" -> BOTH
                "client" -> CLIENT
                "server" -> SERVER
                else -> throw LoadError.BadSide(string)
            }
        }

        fun fromSideCompat(client: SideCompatJson, server: SideCompatJson): Side {
            return if (client == SideCompatJson.UNSUPPORTED) {
                if (server == SideCompatJson.UNSUPPORTED) {
                    // ??? This shouldn't happen
                    throw IllegalStateException("Encountered a mod not supported on client or server")
                } else {
                    SERVER
                }
            } else {
                if (server == SideCompatJson.UNSUPPORTED) {
                    CLIENT
                } else {
                    BOTH
                }
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            BOTH -> "both"
            CLIENT -> "client"
            SERVER -> "server"
        }
    }
}
