package com.kneelawk.mrmpb.ui.util.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.kneelawk.mrmpb.GlobalSettings
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.layout.ContainerBox
import java.nio.file.Path
import java.nio.file.Paths

val HOME_FOLDER: Path = Paths.get(System.getProperty("user.home"))

@Composable
fun OpenFileDialog(
    title: String, initialFolder: Path = HOME_FOLDER, initialSelection: String = "", finished: (Path?) -> Unit
) {
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

    FileChooserDialog(
        title = title,
        mode = FileChooserMode.OPEN_FILE,
        initialFolder = initialFolder,
        initialSelection = initialSelection,
        finished = finished
    )
}

@Composable
fun OpenDirectoryDialog(
    title: String, initialFolder: Path = HOME_FOLDER, initialSelection: String = "", finished: (Path?) -> Unit
) {
    FileChooserDialog(
        title = title,
        mode = FileChooserMode.OPEN_DIRECTORY,
        initialFolder = initialFolder,
        initialSelection = initialSelection,
        finished = finished
    )
}

enum class FileChooserMode {
    SAVE, OPEN_FILE, OPEN_DIRECTORY
}

@Composable
fun FileChooserDialog(
    title: String, mode: FileChooserMode, initialFolder: Path = HOME_FOLDER, initialSelection: String = "",
    finished: (Path?) -> Unit
) {
    val state = rememberDialogState(size = DpSize(1280.dp, 800.dp))

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
                FileChooserView(
                    rememberFileChooserController(
                        mode = mode,
                        initialFolder = initialFolder,
                        initialSelection = initialSelection,
                        finished = finished
                    )
                )
            }
        }
    }
}
