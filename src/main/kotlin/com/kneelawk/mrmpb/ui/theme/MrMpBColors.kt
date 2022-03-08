package com.kneelawk.mrmpb.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

fun lightMrMpBColors(headingColor: Color = Color.Black) = MrMpBColors(
    headingColor,
    true
)

fun darkMrMpBColors(headingColor: Color = Color.White) = MrMpBColors(
    headingColor,
    false
)

class MrMpBColors(headingColor: Color, isLight: Boolean) {
    val headingColor by mutableStateOf(headingColor, structuralEqualityPolicy())
    val isLight by mutableStateOf(isLight, structuralEqualityPolicy())
}

val MrMpBLocalColors = staticCompositionLocalOf { lightMrMpBColors() }
