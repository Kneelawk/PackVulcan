package com.kneelawk.packvulcan.ui.attributor

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.kneelawk.packvulcan.GlobalSettings
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.layout.DialogContainerBox
import com.kneelawk.packvulcan.ui.util.widgets.TextArea

@Composable
fun AttributorView(onCloseRequest: () -> Unit, modsList: List<PackwizMod>) {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    Window(state = state, title = "Attribution Generator", onCloseRequest = onCloseRequest) {
        PackVulcanTheme(GlobalSettings.darkMode) {
            DialogContainerBox {
                AttributorView(rememberAttributorController(modsList))
            }
        }
    }
}

@Composable
fun AttributorView(controller: AttributorInterface) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = controller::generateAttributions, modifier = Modifier.fillMaxWidth()) {
            Text("Generate Attributions")
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            TextArea(
                controller.attributionText, onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                enabled = !controller.loading
            )

            if (controller.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
