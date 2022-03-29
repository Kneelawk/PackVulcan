package com.kneelawk.mrmpb.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

fun lightMrMpBColors(headingColor: Color = Color.Black, linkColor: Color = Color.Blue) = MrMpBColors(
    headingColor,
    linkColor,
    true
)

fun darkMrMpBColors(headingColor: Color = Color.White, linkColor: Color = Color.Blue) = MrMpBColors(
    headingColor,
    linkColor,
    false
)

class MrMpBColors(headingColor: Color, linkColor: Color, isLight: Boolean) {
    val headingColor by mutableStateOf(headingColor, structuralEqualityPolicy())
    val linkColor by mutableStateOf(linkColor, structuralEqualityPolicy())
    val isLight by mutableStateOf(isLight, structuralEqualityPolicy())
}

val MrMpBLocalColors = staticCompositionLocalOf { lightMrMpBColors() }
