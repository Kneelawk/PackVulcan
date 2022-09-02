package com.kneelawk.packvulcan.engine.packwiz

import com.kneelawk.packvulcan.engine.modinfo.ModInfo
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.model.packwiz.mod.ModToml
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    override val filePath: String, override val alias: String?, override val preserve: Boolean, val toml: ModToml,
    private var info: SimpleModInfo? = null
) : PackwizFile, PackwizMod {
    override val displayName: String
        get() = toml.name
    override val provider by lazy { ModInfo.getModProvider(this) }

    private val infoMutex = Mutex()

    override suspend fun getSimpleInfo(): SimpleModInfo? {
        // quick return path
        val info1 = info
        if (info1 != null) return info1

        infoMutex.withLock {
            // synchronize everything and try again
            val info2 = info
            if (info2 != null) return info2

            // we *actually* don't have info, so we'll retrieve it now
            val info3 = ModInfo.getSimpleInfo(this)
            info = info3
            return info3
        }
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
