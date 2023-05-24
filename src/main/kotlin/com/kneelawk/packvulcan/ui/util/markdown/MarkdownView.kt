package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kneelawk.packvulcan.ui.theme.PackVulcanColors
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.theme.PackVulcanTypography
import com.kneelawk.packvulcan.util.LoadingState
import com.vladsch.flexmark.parser.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MarkdownView(text: String) {
    var document by remember { mutableStateOf<LoadingState<MDBlock>>(LoadingState.Loading) }

    val ctx = rememberMDContext()

    LaunchedEffect(text, ctx) {
        println(text)
        println()
        withContext(Dispatchers.IO) {
            val parser = Parser.builder().build()
            val parsed = MDBlock.parse(parser.parse(text), ctx)
            withContext(Dispatchers.Main) {
                println(parsed.toString())
                document = LoadingState.Loaded(parsed)
            }
        }
    }

    Crossfade(
        targetState = document, modifier = Modifier.fillMaxSize()
    ) { state ->
        when (state) {
            LoadingState.Error -> {}
            is LoadingState.Loaded -> state.data.render()
            LoadingState.Loading -> CircularProgressIndicator()
        }
    }
}

interface MDNode {
    @Composable
    fun render()
}

data class MDContext(
    val typography: Typography, val colors: Colors, val pvTypography: PackVulcanTypography,
    val pvColors: PackVulcanColors
)

@Composable
fun rememberMDContext(
    typography: Typography = MaterialTheme.typography, colors: Colors = MaterialTheme.colors,
    pvTypography: PackVulcanTypography = PackVulcanTheme.typography, pvColors: PackVulcanColors = PackVulcanTheme.colors
): MDContext =
    remember(typography, colors, pvTypography, pvColors) { MDContext(typography, colors, pvTypography, pvColors) }
