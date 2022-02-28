package com.kneelawk.mrmpb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Green700,
    primaryVariant = Green800,
    secondary = Gray800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    background = Gray950,
    surface = Gray900,
)

private val LightColorPalette = lightColors(
    primary = Green700,
    primaryVariant = Green800,
    secondary = BlueGray100,
    background = BlueGray50,
    surface = Color.White
)

@Composable
fun MrMpBTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(colors = colors, typography = Typography, shapes = Shapes, content = content)
}