package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.ui.util.ModIconWrapper
import com.kneelawk.packvulcan.util.LoadingState

@Composable
fun ModIcon(modImage: LoadingState<ModIconWrapper>, reload: () -> Unit) {
    Box(modifier = Modifier.size(ImageUtils.MOD_ICON_SIZE.dp)) {
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
                        .size(ImageUtils.MOD_ICON_SIZE.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
            }
        }
    }
}
