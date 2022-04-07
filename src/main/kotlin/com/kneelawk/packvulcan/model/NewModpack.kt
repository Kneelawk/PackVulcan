package com.kneelawk.packvulcan.model

import java.nio.file.Path

/**
 * Contains only the information provided from the CreateNew dialog.
 */
data class NewModpack(
    val location: Path, val name: String, val author: String, val version: String,
    val minecraftVersion: MinecraftVersion, val loaderVersion: LoaderVersion
)
