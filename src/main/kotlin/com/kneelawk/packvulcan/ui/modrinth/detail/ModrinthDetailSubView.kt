package com.kneelawk.packvulcan.ui.modrinth.detail

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kneelawk.packvulcan.model.ModrinthModInfo
import com.kneelawk.packvulcan.ui.detail.DetailSubView
import com.kneelawk.packvulcan.ui.util.ModIconWrapper
import com.kneelawk.packvulcan.util.LoadingState

class ModrinthDetailSubView(val mod: ModrinthModInfo) : DetailSubView {
    override val title = mod.name
    override val description = mod.description
    override val supportsGallery = true
    override val supportsVersions = true
    override var modIcon by mutableStateOf<LoadingState<ModIconWrapper>>(LoadingState.Loading)

    override suspend fun loadModIcon() {
        modIcon = try {
            LoadingState.Loaded(ModIconWrapper.fromIconSource(mod.icon))
        } catch (e: Exception) {
            LoadingState.Error
        }
    }

    @Composable
    override fun doBody() {
        Text(mod.body)
    }

    @Composable
    override fun doGallery() {
    }

    @Composable
    override fun doVersions() {
    }
}
