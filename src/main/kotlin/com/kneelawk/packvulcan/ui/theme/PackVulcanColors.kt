package com.kneelawk.packvulcan.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

fun lightPackVulcanColors(headingColor: Color = Color.Black, linkColor: Color = Color.Blue) = PackVulcanColors(
    headingColor,
    linkColor,
    true
)

fun darkPackVulcanColors(headingColor: Color = Color.White, linkColor: Color = Color.Blue) = PackVulcanColors(
    headingColor,
    linkColor,
    false
)

class PackVulcanColors(headingColor: Color, linkColor: Color, isLight: Boolean) {
    val headingColor by mutableStateOf(headingColor, structuralEqualityPolicy())
    val linkColor by mutableStateOf(linkColor, structuralEqualityPolicy())
    val isLight by mutableStateOf(isLight, structuralEqualityPolicy())
}

val PackVulcanLocalColors = staticCompositionLocalOf { lightPackVulcanColors() }
