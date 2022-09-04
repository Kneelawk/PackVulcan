package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.modrinth.install.InstallRequest
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton

@Composable
fun InstallButton(
    state: InstallerState, request: InstallRequest, modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small, enabled: Boolean = true, content: @Composable RowScope.() -> Unit
) {
    DisposableEffect(state, request) {
        state.buttonCompose(request)

        onDispose {
            state.buttonDecompose(request)
        }
    }

    Box(contentAlignment = Alignment.Center) {
        val loading = state.loading(request)
        SmallButton(
            onClick = { state.startInstall(request) },
            modifier = modifier,
            shape = shape,
            enabled = enabled && !loading
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(state.loadingMsg(request), modifier = Modifier.padding(start = 5.dp))
            } else {
                content()
            }
        }

        state.showButtonDependencyPopup(request)
    }
}
