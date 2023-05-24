package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.util.LoadingState
import com.vladsch.flexmark.parser.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MarkdownView(text: String) {
    var document by remember { mutableStateOf<LoadingState<MDBlock>>(LoadingState.Loading) }

    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colors
    val pvColors = PackVulcanTheme.colors
    val pvTypography = PackVulcanTheme.typography

    LaunchedEffect(text, typography, colors) {
        println(text)
        println()
        withContext(Dispatchers.IO) {
            val parser = Parser.builder().build()
            val parsed = MDBlock.parse(parser.parse(text), typography, colors, pvTypography, pvColors)
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
