package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun OpenFileDialog(title: String, finished: (String?) -> Unit) = AwtWindow(
    create = {
        object : FileDialog(null as? Frame, title, LOAD) {
            override fun setVisible(b: Boolean) {
                super.setVisible(b)
                if (b) {
                    finished(file)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)