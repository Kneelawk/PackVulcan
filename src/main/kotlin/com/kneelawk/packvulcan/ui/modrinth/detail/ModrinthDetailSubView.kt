package com.kneelawk.packvulcan.ui.modrinth.detail

import androidx.compose.runtime.Composable
import com.kneelawk.packvulcan.model.ModrinthModInfo
import com.kneelawk.packvulcan.ui.detail.DetailSubView

class ModrinthDetailSubView(val mod: ModrinthModInfo) : DetailSubView {
    override val title = mod.name
    override val supportsGallery = true
    override val supportsVersions = true

    @Composable
    override fun doBody() {
    }

    @Composable
    override fun doGallery() {
    }

    @Composable
    override fun doVersions() {
    }
}
