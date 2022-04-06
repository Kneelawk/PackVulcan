package com.kneelawk.mrmpb.engine.modinfo

import com.kneelawk.mrmpb.model.modfile.fabric.FabricModJson
import com.kneelawk.mrmpb.model.modfile.forge.ModsToml
import com.kneelawk.mrmpb.model.modfile.quilt.QuiltModJson
import java.awt.image.BufferedImage

class ModFileInfo(val metadata: ModFileMetadata, val icon: BufferedImage?)

sealed class ModFileMetadata {
    data class Fabric(val data: FabricModJson) : ModFileMetadata()
    data class Forge(val data: ModsToml) : ModFileMetadata()
    data class Quilt(val data: QuiltModJson) : ModFileMetadata()
}
