package com.kneelawk.mrmpb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Green400,
    primaryVariant = Green600,
    secondary = Gray800,
    onPrimary = Color.Black,
    onSecondary = Gray200,
    background = Gray950,
    surface = Gray900,
    onBackground = Gray200,
    onSurface = Gray200,
)

private val DarkColorPaletteExtended = darkMrMpBColors(
    headingColor = Cyan50,
    linkColor = Blue300,
)

private val LightColorPalette = lightColors(
    primary = BlueGreen400,
    primaryVariant = BlueGreen600,
    secondary = BlueGray100,
    background = BlueGray50,
    surface = Color.White
)

private val LightColorPaletteExtended = lightMrMpBColors(
    linkColor = Blue600,
)

@Composable
fun MrMpBTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val extendedColors = if (darkTheme) {
        DarkColorPaletteExtended
    } else {
        LightColorPaletteExtended
    }

    CompositionLocalProvider(MrMpBLocalColors provides extendedColors) {
        MaterialTheme(colors = colors, typography = Typography, shapes = Shapes, content = content)
    }
}

object MrMpBTheme {
    val colors: MrMpBColors
        @Composable
        @ReadOnlyComposable
        get() = MrMpBLocalColors.current
}
