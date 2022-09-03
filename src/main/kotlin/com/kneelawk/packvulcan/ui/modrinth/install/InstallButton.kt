package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.ui.InstallOperation
import com.kneelawk.packvulcan.ui.util.widgets.Dropdown
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton
import kotlinx.coroutines.CoroutineScope

@Composable
fun InstallButton(
    display: InstallDisplay, acceptableVersions: AcceptableVersions, installedProjects: Set<String>,
    install: (InstallOperation) -> Unit, onLoading: (loading: Boolean) -> Unit = {},
    installScope: CoroutineScope = rememberCoroutineScope(), autoInstall: Boolean = true,
    beforeDialog: suspend () -> Unit = {}, shape: Shape = MaterialTheme.shapes.small, modifier: Modifier = Modifier,
    enabled: Boolean = true, content: @Composable RowScope.() -> Unit
) {
    InstallButton(
        rememberInstallController(
            display = display,
            acceptableVersions = acceptableVersions,
            installedProjects = installedProjects,
            install = install,
            onLoading = onLoading,
            installScope = installScope,
            autoInstall = autoInstall,
            beforeDialog = beforeDialog,
            shape = shape,
            modifier = modifier,
            enabled = enabled,
            content = content
        )
    )
}

@Composable
private fun InstallButton(controller: InstallInterface) {
    Box(contentAlignment = Alignment.Center) {
        SmallButton(
            onClick = { controller.startInstall() },
            modifier = controller.buttonModifier,
            shape = controller.buttonShape,
            enabled = controller.enabled && !controller.loading
        ) {
            if (controller.loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(controller.loadingText, modifier = Modifier.padding(start = 5.dp))
            } else {
                with(controller) {
                    content()
                }
            }
        }

        Dropdown(
            expanded = controller.dialogOpen,
            onDismissRequest = controller::cancelDialog
        ) {
            InstallDialog(controller)
        }
    }
}
