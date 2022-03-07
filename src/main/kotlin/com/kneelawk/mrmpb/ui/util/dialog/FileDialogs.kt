package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.mrmpb.ui.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.ContainerBox
import java.nio.file.Path

@Composable
fun OpenFileDialog(title: String, finished: (Path?) -> Unit) {
//    AwtWindow(
//        create = {
//            object : FileDialog(null as? Frame, title, LOAD) {
//                override fun setVisible(b: Boolean) {
//                    super.setVisible(b)
//                    if (b) {
//                        finished(file)
//                    }
//                }
//            }
//        }, dispose = FileDialog::dispose
//    )

    FileChooserDialog(title, FileChooserMode.OPEN_FILE, finished)
}

@Composable
fun OpenDirectoryDialog(title: String, finished: (Path?) -> Unit) {
    FileChooserDialog(title, FileChooserMode.OPEN_DIRECTORY, finished)
}

enum class FileChooserMode {
    SAVE, OPEN_FILE, OPEN_DIRECTORY
}

@Composable
fun FileChooserDialog(title: String, mode: FileChooserMode, finished: (Path?) -> Unit) {
    val state = rememberDialogState(size = DpSize(1280.dp, 720.dp))

    Dialog(title = title, state = state, onCloseRequest = {
        finished(null)
    }) {
//        SwingPanel(modifier = Modifier.fillMaxSize(), factory = {
//            JFileChooser().apply {
//                SwingUtilities.updateComponentTreeUI(this)
//                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
//                addActionListener {
//                    if (it.actionCommand == JFileChooser.APPROVE_SELECTION) {
//                        finished(selectedFile)
//                    } else if (it.actionCommand == JFileChooser.CANCEL_SELECTION) {
//                        finished(null)
//                    }
//                }
//            }
//        })

        MrMpBTheme(GlobalSettings.darkMode) {
            ContainerBox {
                FileChooserView(rememberFileChooserController(mode, finished))
            }
        }
    }
}
