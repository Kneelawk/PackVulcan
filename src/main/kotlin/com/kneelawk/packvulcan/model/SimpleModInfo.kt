package com.kneelawk.packvulcan.model

sealed interface SimpleModInfo {
    val name: String
    val author: String
    val description: String?
    val icon: IconSource?
    val projectUrl: String?

    data class Modrinth(
        override val name: String, override val author: String, override val description: String,
        override val icon: IconSource?, override val projectUrl: String, override val projectId: String,
        override val slug: String, override val body: String
    ) : SimpleModInfo, ModrinthModInfo

    data class Curseforge(
        override val name: String, override val author: String, override val description: String?,
        override val icon: IconSource?, override val projectUrl: String, override val projectId: Long,
        override val slug: String
    ) : SimpleModInfo, CurseforgeModInfo
}

sealed interface ModrinthModInfo : SimpleModInfo {
    override val projectUrl: String
    override val description: String
    val projectId: String
    val slug: String
    val body: String
}

sealed interface CurseforgeModInfo : SimpleModInfo {
    override val projectUrl: String
    val projectId: Long
    val slug: String
}
