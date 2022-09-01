package com.kneelawk.packvulcan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.dialog.LoaderVersionDialog
import com.kneelawk.packvulcan.ui.util.dialog.MinecraftVersionDialog
import com.kneelawk.packvulcan.ui.util.dialog.file.OpenDirectoryDialog
import com.kneelawk.packvulcan.ui.util.layout.Form
import com.kneelawk.packvulcan.ui.util.widgets.*
import java.nio.file.Paths
import kotlin.io.path.pathString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModpackDetailsView(
    location: String, locationChange: ((String) -> Unit)?, name: String, nameChange: (String) -> Unit, author: String,
    authorChange: (String) -> Unit, version: String, versionChange: (String) -> Unit, versionError: Boolean,
    minecraftVersion: String, minecraftVersionChange: (String) -> Unit, minecraftVersionError: Boolean,
    loaderVersion: String, loaderVersionChange: (String) -> Unit, loaderVersionError: Boolean,
    additionalLoaders: List<LoaderVersion.Type>, loaderSelected: (LoaderVersion.Type) -> Boolean,
    toggleLoader: (LoaderVersion.Type) -> Unit, additionalMinecraftText: String,
    additionalMinecraftTextChange: (String) -> Unit, additionalMinecraftVersions: List<String>,
    additionalMinecraftOptions: List<String>, addAdditionalMinecraft: (String) -> Unit,
    removeAdditionalMinecraft: (String) -> Unit, additionalMinecraftTextError: Boolean, enabled: Boolean = true
) {
    val projectLocationEditable = locationChange != null
    var projectLocationDialog by remember { mutableStateOf(false) }
    var minecraftVersionDialog by remember { mutableStateOf(false) }
    var loaderVersionDialog by remember { mutableStateOf(false) }
    var extraExpanded by remember { mutableStateOf(false) }

    if (projectLocationDialog && projectLocationEditable) {
        val initialFolder = if (location.isBlank()) {
            Paths.get(System.getProperty("user.home"))
        } else {
            Paths.get(location).normalize().parent ?: Paths.get(System.getProperty("user.home"))
        }
        OpenDirectoryDialog(
            title = "Select a project location...", initialFolder = initialFolder, initialSelection = location
        ) { selection ->
            projectLocationDialog = false
            selection?.let { locationChange!!(it.pathString) }
        }
    }

    if (minecraftVersionDialog) {
        MinecraftVersionDialog("Select a Minecraft version...", minecraftVersion) { selection ->
            minecraftVersionDialog = false
            selection?.let { minecraftVersionChange(it.toString()) }
        }
    }

    if (loaderVersionDialog) {
        LoaderVersionDialog("Select a mod loader...", loaderVersion, minecraftVersion) { selection ->
            loaderVersionDialog = false
            selection?.let { loaderVersionChange(it.toString()) }
        }
    }

    Column {
        Form(
            rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), columnSpacing = 10.dp,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                "File", modifier = Modifier.formSection(), style = MaterialTheme.typography.h5,
                color = PackVulcanTheme.colors.headingColor
            )

            Text("Project Location:", modifier = Modifier.formLabel())
            SmallTextField(location, locationChange ?: {}, modifier = Modifier.formField(), enabled = enabled)
            if (projectLocationEditable) {
                SmallButton(
                    onClick = { projectLocationDialog = true }, modifier = Modifier.formConfigure(), enabled = enabled
                ) {
                    Text("...")
                }
            }

            Text(
                "Details", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
                color = PackVulcanTheme.colors.headingColor
            )

            Text("Modpack Name:", modifier = Modifier.formLabel())
            SmallTextField(name, nameChange, modifier = Modifier.formField(), enabled = enabled)

            Text("Modpack Author:", modifier = Modifier.formLabel())
            SmallTextField(author, authorChange, modifier = Modifier.formField(), enabled = enabled)

            Text("Modpack Version:", modifier = Modifier.formLabel())
            SmallTextField(
                version, versionChange, modifier = Modifier.formField(), isError = versionError, enabled = enabled
            )

            Text(
                "Versions", modifier = Modifier.formSection().padding(top = 10.dp), style = MaterialTheme.typography.h5,
                color = PackVulcanTheme.colors.headingColor
            )

            Text("Minecraft Version:", modifier = Modifier.formLabel())
            SmallTextField(
                minecraftVersion, minecraftVersionChange, modifier = Modifier.formField(),
                isError = minecraftVersionError, enabled = enabled
            )
            SmallButton(
                onClick = { minecraftVersionDialog = true }, modifier = Modifier.formConfigure(), enabled = enabled
            ) {
                Text("...")
            }

            Text("Loader Version:", modifier = Modifier.formLabel())
            SmallTextField(
                loaderVersion, loaderVersionChange, modifier = Modifier.formField(), isError = loaderVersionError,
                enabled = enabled
            )
            SmallButton(
                onClick = { loaderVersionDialog = true }, modifier = Modifier.formConfigure(), enabled = enabled
            ) {
                Text("...")
            }
        }

        Divider()

        AnimatedVisibility(visible = extraExpanded) {
            Form(
                rowArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), columnSpacing = 10.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    "Extra Acceptable Versions", modifier = Modifier.formSection().padding(top = 10.dp),
                    style = MaterialTheme.typography.h5,
                    color = PackVulcanTheme.colors.headingColor
                )

                Text("Minecraft:", modifier = Modifier.formLabel())
                ChipField(
                    text = additionalMinecraftText,
                    onTextChange = additionalMinecraftTextChange,
                    chips = additionalMinecraftVersions,
                    chipOptions = additionalMinecraftOptions,
                    addChip = addAdditionalMinecraft,
                    removeChip = removeAdditionalMinecraft,
                    isError = additionalMinecraftTextError,
                    modifier = Modifier.formField()
                )

                Text("Loader:", modifier = Modifier.formLabel())
                Row(
                    modifier = Modifier.formField()
                        .background(
                            TextFieldDefaults.textFieldColors().backgroundColor(enabled).value,
                            MaterialTheme.shapes.small
                        )
                        .defaultMinSize(SmallTextFieldDefaults.MinWidth, SmallTextFieldDefaults.MinHeight)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (loader in additionalLoaders) {
                        key(loader) {
                            SmallFilterChip(
                                selected = loaderSelected(loader), onClick = { toggleLoader(loader) },
                                enabled = enabled,
                                selectedIcon = { Icon(Icons.Default.CheckCircle, "selected") },
                                leadingIcon = { Icon(Icons.Default.AddCircle, "add loader") }
                            ) {
                                Text(loader.prettyName)
                            }
                        }
                    }
                }
            }
        }

        SmallTextButton(
            onClick = {
                extraExpanded = !extraExpanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val iconRotation by animateFloatAsState(if (extraExpanded) -180f else 0f)
            Icon(
                Icons.Default.ArrowDropDown, "drop-down",
                modifier = Modifier.rotate(iconRotation)
            )
            Text("More", Modifier.padding(start = 5.dp))
        }
    }
}
