package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.ui.InstallOperation
import com.kneelawk.packvulcan.ui.util.widgets.DialogButtonBar
import com.kneelawk.packvulcan.util.MSet

@Composable
fun InstallView(
    display: InstallDisplay, acceptableVersions: AcceptableVersions, installedProjects: MSet<String>,
    onCloseRequest: () -> Unit, install: (InstallOperation) -> Unit
) {
    InstallView(
        rememberInstallController(
            display = display,
            acceptableVersions = acceptableVersions,
            installedProjects = installedProjects,
            onCloseRequest = onCloseRequest,
            install = install
        )
    )
}

@Composable
private fun InstallView(controller: InstallInterface) {
    Box(modifier = Modifier.size(480.dp, 270.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            ) {
                Text(controller.modName, fontWeight = FontWeight.Bold)
                Text(controller.modVersion)
            }

            Divider()

            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colors.background)) {
                val scrollState = rememberLazyListState()

                LazyColumn(
                    state = scrollState, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(controller.collectedDependencies) {
                        DependencyView(it)
                    }
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                if (controller.collectedDependencies.isEmpty()) {
                    Text("No dependencies.", modifier = Modifier.align(Alignment.Center))
                }
            }

            Divider()

            DialogButtonBar(
                onCancel = controller::cancel,
                onConfirm = controller::install,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                confirmEnabled = !controller.loading
            )
        }

        LoadingView(
            controller.loading, controller.loadingText,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DependencyView(display: DependencyDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { display.install.value = !display.install.value }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(display.mod.name, fontWeight = FontWeight.Bold)
            Text(display.mod.version)

            Spacer(Modifier.weight(1f))

            Checkbox(
                checked = display.install.value,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
        }
    }
}
