package com.kneelawk.mrmpb.engine.packwiz

import com.kneelawk.mrmpb.model.packwiz.mod.ModToml
import java.nio.file.Path

sealed class PackwizFile {
    abstract val alias: String?
    abstract val preserve: Boolean

    data class MetaFile(override val alias: String?, override val preserve: Boolean, val toml: ModToml) : PackwizFile()
    data class RealFile(override val alias: String?, override val preserve: Boolean, val file: Path) : PackwizFile()
}
