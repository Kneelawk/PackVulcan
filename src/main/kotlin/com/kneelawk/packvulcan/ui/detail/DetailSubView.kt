package com.kneelawk.packvulcan.ui.detail

import androidx.compose.runtime.Composable

interface DetailSubView {
    val title: String

    val supportsGallery: Boolean
    val supportsVersions: Boolean

    @Composable
    fun doBody()

    @Composable
    fun doGallery()

    @Composable
    fun doVersions()
}
