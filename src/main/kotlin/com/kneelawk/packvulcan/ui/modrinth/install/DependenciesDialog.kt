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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.widgets.DialogButtonBar
import com.kneelawk.packvulcan.ui.util.widgets.Dropdown

@Composable
fun DependenciesDialog(
    modName: String,
    modVersion: String,
    dependencies: List<SimpleModInfo>,
    onCancel: () -> Unit,
    onInstall: (selectedDeps: List<SimpleModInfo>) -> Unit
) {
    val state = rememberDialogState(width = 480.dp, height = 270.dp)

    Dialog(
        title = "Install $modName",
        onCloseRequest = onCancel,
        state = state
    ) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                DependenciesView(
                    modName = modName,
                    modVersion = modVersion,
                    dependencies = dependencies,
                    onCancel = onCancel,
                    onInstall = onInstall
                )
            }
        }
    }
}

@Composable
fun DependenciesPopup(
    expanded: Boolean,
    modName: String,
    modVersion: String,
    dependencies: List<SimpleModInfo>,
    onCancel: () -> Unit,
    onInstall: (selectedDeps: List<SimpleModInfo>) -> Unit
) {
    Dropdown(
        expanded = expanded,
        onDismissRequest = onCancel
    ) {
        DependenciesView(
            modName = modName,
            modVersion = modVersion,
            dependencies = dependencies,
            onCancel = onCancel,
            onInstall = onInstall
        )
    }
}

@Composable
fun DependenciesView(
    modName: String,
    modVersion: String,
    dependencies: List<SimpleModInfo>,
    onCancel: () -> Unit,
    onInstall: (selectedDeps: List<SimpleModInfo>) -> Unit
) {
    val collectedDependencies = remember(dependencies) {
        dependencies.map { DependencyDisplay(it, mutableStateOf(true)) }
    }

    Column(modifier = Modifier.size(480.dp, 270.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(modName, fontWeight = FontWeight.Bold)
            Text(modVersion)
        }

        Divider()

        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colors.background)) {
            val scrollState = rememberLazyListState()

            LazyColumn(
                state = scrollState, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(collectedDependencies) {
                    DependencyView(it)
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            if (collectedDependencies.isEmpty()) {
                Text("No dependencies.", modifier = Modifier.align(Alignment.Center))
            }
        }

        Divider()

        DialogButtonBar(
            onCancel = onCancel,
            onConfirm = {
                onInstall(collectedDependencies.asSequence().filter { it.install.value }.map { it.mod }.toList())
            },
            modifier = Modifier.fillMaxWidth().padding(10.dp)
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
