package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.ui.util.IconWrapper
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReloadableIcon(
    modImage: LoadingState<IconWrapper>,
    modifier: Modifier = Modifier.size(ImageUtils.MOD_ICON_SIZE.dp),
    shape: Shape = RoundedCornerShape(5.dp),
    reload: () -> Unit
) {
    Box(modifier = modifier) {
        when (modImage) {
            LoadingState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LoadingState.Error -> {
                IconButton(onClick = reload, modifier = Modifier.align(Alignment.Center)) {
                    Icon(Icons.Default.Refresh, "reload image")
                }
            }

            is LoadingState.Loaded -> {
                modImage.data.draw(
                    "mod icon",
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxSize()
                        .clip(shape)
                )
            }
        }
    }
}

@Composable
fun AsyncIcon(
    source: IconSource?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(5.dp),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var modImage by remember { mutableStateOf<LoadingState<IconWrapper>>(LoadingState.Loading) }

    suspend fun loadModIcon(source: IconSource?) {
        modImage = LoadingState.Loaded(IconWrapper.fromIconSource(source))
    }

    LaunchedEffect(source) {
        loadModIcon(source)
    }

    ReloadableIcon(
        modImage = modImage,
        modifier = modifier,
        shape = shape
    ) { scope.launch { loadModIcon(source) } }
}
