package com.kneelawk.packvulcan.engine.packwiz

import com.kneelawk.packvulcan.engine.modinfo.ModInfo
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.packwiz.mod.ModToml
import java.nio.file.Path

sealed interface PackwizFile {
    val filePath: String
    val alias: String?
    val preserve: Boolean
}

sealed interface PackwizMod : PackwizFile {
    val displayName: String
    val provider: ModProvider

    suspend fun getSimpleInfo(): SimpleModInfo?
}

data class PackwizMetaFile(
    override val filePath: String, override val alias: String?, override val preserve: Boolean, val toml: ModToml
) : PackwizFile, PackwizMod {
    override val displayName: String
        get() = toml.name
    override val provider by lazy { ModInfo.getModProvider(this) }

    override suspend fun getSimpleInfo(): SimpleModInfo? {
        return ModInfo.getSimpleInfo(this)
    }
}

sealed interface PackwizRealFile : PackwizFile {
    val file: Path
}

data class PackwizModFile(
    override val filePath: String, override val alias: String?, override val preserve: Boolean, override val file: Path,
    val info: SimpleModInfo
) : PackwizRealFile, PackwizMod {
    override val displayName: String
        get() = info.name
    override val provider: ModProvider
        get() = ModProvider.FILESYSTEM

    override suspend fun getSimpleInfo(): SimpleModInfo {
        return info
    }
}

data class PackwizResourceFile(
    override val filePath: String, override val alias: String?, override val preserve: Boolean, override val file: Path
) : PackwizRealFile
