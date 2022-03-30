package com.kneelawk.mrmpb.model.packwiz

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
    }

    override fun toString(): String {
        return when (this) {
            BOTH -> "both"
            CLIENT -> "client"
            SERVER -> "server"
        }
    }
}