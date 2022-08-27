package com.kneelawk.packvulcan.ui.util.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogButtonBar(
    modifier: Modifier = Modifier, onCancel: () -> Unit, onConfirm: () -> Unit, confirmEnabled: Boolean = true,
    cancelContent: @Composable RowScope.() -> Unit = {
        ButtonIcon(Icons.Default.Close, "cancel")
        Text("Cancel", modifier = Modifier.padding(start = 5.dp))
    },
    confirmContent: @Composable RowScope.() -> Unit = {
        ButtonIcon(Icons.Default.Check, "confirm")
        Text("Select", modifier = Modifier.padding(start = 5.dp))
    }
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
    ) {
        SmallButton(onClick = {
            onCancel()
        }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) {
            cancelContent()
        }

        SmallButton(onClick = {
            onConfirm()
        }, enabled = confirmEnabled) {
            confirmContent()
        }
    }
}
