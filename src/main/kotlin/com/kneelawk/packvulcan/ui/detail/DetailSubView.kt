package com.kneelawk.packvulcan.ui.detail

import androidx.compose.runtime.Composable
import com.kneelawk.packvulcan.ui.util.IconWrapper
import com.kneelawk.packvulcan.util.LoadingState

interface DetailSubView {
    val title: String
    val description: String

    val supportsGallery: Boolean
    val supportsVersions: Boolean
    val modIcon: LoadingState<IconWrapper>

    suspend fun loadModIcon()

    @Composable
    fun doBody()

    @Composable
    fun doGallery()

    @Composable
    fun doVersions()
}
