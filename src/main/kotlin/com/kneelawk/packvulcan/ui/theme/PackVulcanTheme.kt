package com.kneelawk.packvulcan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = BlueGreen500,
    primaryVariant = BlueGreen700,
    secondary = Gray800,
    onPrimary = Color.Black,
    onSecondary = Gray200,
    background = Gray950,
    surface = Gray900,
    onBackground = Gray200,
    onSurface = Gray200,
)

private val DarkColorPaletteExtended = darkPackVulcanColors(
    headingColor = Cyan50,
    linkColor = Blue300,
)

private val LightColorPalette = lightColors(
    primary = BlueGreen700,
    primaryVariant = BlueGreen900,
    secondary = BlueGray100,
    background = BlueGray50,
    surface = Color.White
)

private val LightColorPaletteExtended = lightPackVulcanColors(
    linkColor = Blue600,
)

@Composable
fun PackVulcanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
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

    CompositionLocalProvider(PackVulcanLocalColors provides extendedColors) {
        MaterialTheme(colors = colors, typography = Typography, shapes = Shapes, content = content)
    }
}

object PackVulcanTheme {
    val colors: PackVulcanColors
        @Composable
        @ReadOnlyComposable
        get() = PackVulcanLocalColors.current
}
