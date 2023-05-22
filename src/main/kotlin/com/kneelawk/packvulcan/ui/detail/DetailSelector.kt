package com.kneelawk.packvulcan.ui.detail

import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.ModrinthModInfo
import com.kneelawk.packvulcan.ui.modrinth.detail.ModrinthDetailSubView

sealed interface DetailSelector {
    val viewType: ViewType
    val provider: ModProvider

    suspend fun load(): DetailSubView?
}

data class PackwizFileSel(val mod: PackwizMod, override val viewType: ViewType) : DetailSelector {
    override val provider: ModProvider
        get() = mod.provider

    override suspend fun load(): DetailSubView? {
        return mod.getSimpleInfo()?.let {
            when (it) {
                is ModrinthModInfo -> ModrinthDetailSubView(it)
                else -> null
            }
        }
    }
}

data class ModrinthProjectSel(val projectId: String) : DetailSelector {
    override val viewType = ViewType.BODY
    override val provider = ModProvider.MODRINTH

    override suspend fun load(): DetailSubView? {
        return ModrinthUtils.getModrinthProjectInfo(projectId)?.let(::ModrinthDetailSubView)
    }
}
