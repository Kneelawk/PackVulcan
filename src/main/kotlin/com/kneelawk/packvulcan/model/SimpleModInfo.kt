package com.kneelawk.packvulcan.model

sealed class SimpleModInfo {
    abstract val name: String
    abstract val author: String
    abstract val filename: String
    abstract val version: String
    abstract val description: String?
    abstract val icon: ModIcon?
    abstract val projectUrl: String?

    data class Modrinth(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIcon?,
        override val projectUrl: String, val projectId: String, val versionId: String
    ) : SimpleModInfo()

    data class Curseforge(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIcon?,
        override val projectUrl: String, val projectId: Long, val fileId: Long
    ) : SimpleModInfo()

    data class File(
        override val name: String, override val author: String, override val filename: String,
        override val version: String, override val description: String?, override val icon: ModIcon?,
        override val projectUrl: String?, val modId: String
    ) : SimpleModInfo()
}
