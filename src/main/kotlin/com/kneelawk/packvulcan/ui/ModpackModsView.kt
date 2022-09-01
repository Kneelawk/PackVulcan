package com.kneelawk.packvulcan.ui

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.modinfo.ModFileInfo
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.model.ModIconSource
import com.kneelawk.packvulcan.model.ModProvider
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.modrinth.search.ModrinthSearchWindow
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.ui.util.ModIconWrapper
import com.kneelawk.packvulcan.ui.util.dialog.file.OpenFileDialog
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.ui.util.widgets.ModIcon
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private val log = KotlinLogging.logger { }

@Composable
fun ModpackModsDialogs(component: ModpackComponent) {
    if (component.modrinthSearchDialogOpen) {
        ModrinthSearchWindow(
            onCloseRequest = {
                component.modrinthSearchDialogOpen = false
            },
            selectedMinecraftVersions = component.selectedMinecraftVersions,
            selectedKnownLoaders = component.selectedModLoaders,
            modpackName = component.modpackName,
            acceptableVersions = component.acceptableVersions,
            modrinthProjects = component.modrinthProjects,
            openProject = {},
            browseVersions = {}
        )
    }
}

@Composable
fun ModpackModsView(component: ModpackComponent) {
    var addModJarDialogOpen by remember { mutableStateOf(false) }

    if (addModJarDialogOpen) {
        OpenFileDialog(
            title = "Select a mod .jar to add",
            initialFolder = component.previousSelectionDir,
            visibilityFilter = {
                it.isDirectory() || it.name.endsWith(".jar")
            },
            selectionFilter = {
                if (!it.name.endsWith(".jar")) {
                    return@OpenFileDialog "The selected file is not a .jar file."
                }

                ModFileInfo.getFileInfo(it) ?: return@OpenFileDialog "Unable to recognize this .jar as a mod file."

                null
            }
        ) { selected ->
            addModJarDialogOpen = false
            selected?.let {
                component.addModJar(it)
            }
        }
    }

    Column {
        Box(
            modifier = Modifier.fillMaxWidth().shadow(5.dp, RectangleShape, true)
                .background(MaterialTheme.colors.surface)
        ) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                val buttonShape = MaterialTheme.shapes.small
                val startShape = buttonShape.copy(topEnd = CornerSize(0), bottomEnd = CornerSize(0))
                val endShape = buttonShape.copy(topStart = CornerSize(0), bottomStart = CornerSize(0))

                Button(
                    onClick = { component.modrinthSearchDialogOpen = true }, modifier = Modifier.weight(1f),
                    shape = startShape,
                    enabled = !component.loading
                ) {
                    Icon(PackVulcanIcons.modrinth, "modrinth")

                    Text("Add from Modrinth...", modifier = Modifier.padding(start = 10.dp))
                }

                Button(
                    onClick = { addModJarDialogOpen = true }, modifier = Modifier.weight(1f), shape = endShape,
                    enabled = !component.loading
                ) {
                    Icon(PackVulcanIcons.file, "file")

                    Text("Add Mod Jar...", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }

        val lazyListState = rememberLazyListState()

        VerticalScrollWrapper(
            modifier = Modifier.weight(1f).fillMaxWidth(), adapter = ScrollbarAdapter(lazyListState),
            backgroundColor = Color.Transparent, backgroundShape = MaterialTheme.shapes.large,
            scrollbarPadding = PaddingValues(0.dp)
        ) {
            LazyColumn(
                state = lazyListState, modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(20.dp)
            ) {
                items(component.modsList, key = PackwizMod::filePath) { mod ->
                    ModpackModView(component, mod)
                }
            }
        }
    }
}

@Composable
fun ModpackModView(component: ModpackComponent, mod: PackwizMod) {
    val scope = rememberCoroutineScope()

    var modInfo by remember { mutableStateOf<LoadingState<SimpleModInfo>>(LoadingState.Loading) }

    suspend fun loadModInfo() {
        modInfo = supervisorScope {
            try {
                // getting null here means mod data couldn't be loaded
                mod.getSimpleInfo()?.let { LoadingState.Loaded(it) } ?: LoadingState.Error
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.warn("Error getting mod info.", e)
                LoadingState.Error
            }
        }
    }

    LaunchedEffect(Unit) {
        loadModInfo()
    }

    var modImage by remember { mutableStateOf<LoadingState<ModIconWrapper>>(LoadingState.Loading) }

    suspend fun loadModIcon(modInfo: LoadingState<SimpleModInfo>) {
        modImage = when (modInfo) {
            is LoadingState.Loaded -> LoadingState.Loaded(
                when (val icon = modInfo.data.icon) {
                    is ModIconSource.Buffered -> ModIconWrapper.image(icon.image.toComposeImageBitmap())
                    is ModIconSource.Url -> ImageResource.getModIcon(icon.url)?.let { ModIconWrapper.image(it) }
                        ?: ModIconWrapper.icon(ImageWrapper.Painter(PackVulcanIcons.noImage))

                    null -> ModIconWrapper.icon(ImageWrapper.Painter(PackVulcanIcons.noImage))
                }
            )

            LoadingState.Error -> LoadingState.Loaded(ModIconWrapper.icon(PackVulcanIcons.error))
            LoadingState.Loading -> LoadingState.Loading
        }
    }

    LaunchedEffect(modInfo) {
        loadModIcon(modInfo)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            ModIcon(modImage) { scope.launch { loadModIcon(modInfo) } }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        mod.displayName, style = MaterialTheme.typography.h6,
                        color = PackVulcanTheme.colors.headingColor
                    )

                    when (val modInfo = modInfo) {
                        LoadingState.Loading -> {
                            Text("Loading mod author...")
                        }

                        LoadingState.Error -> {
                            Text("Error loading mod author.")
                        }

                        is LoadingState.Loaded -> {
                            Text("by ${modInfo.data.author}")
                        }
                    }
                }

                val providerStr = buildAnnotatedString {
                    append("Provided by: ")
                    pushStyle(SpanStyle(color = providerColor(mod.provider), fontWeight = FontWeight.Bold))
                    append(mod.provider.prettyName)
                    pop()
                }

                Text(providerStr)

                when (val modInfo = modInfo) {
                    LoadingState.Loading -> {
                        Text("Loading mod info...")
                    }

                    LoadingState.Error -> {
                        Button(onClick = {
                            scope.launch {
                                loadModInfo()
                            }
                        }) {
                            Text("Retry")
                        }
                    }

                    is LoadingState.Loaded -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val description = when (val modInfo = modInfo) {
                                LoadingState.Loading -> "Loading description..."
                                LoadingState.Error -> "Error loading description."
                                is LoadingState.Loaded -> modInfo.data.description ?: "No description."
                            }

                            Text(description)

                            Text("Version: ${modInfo.data.version}")
                        }
                    }
                }
            }

            Button(onClick = {
                component.removeMod(mod.filePath)
            }, enabled = !component.loading) {
                Text("Remove")
            }
        }
    }
}

@Composable
fun providerColor(provider: ModProvider): Color {
    return when (provider) {
        ModProvider.MODRINTH -> Color(0xFF30B27B)
        ModProvider.CURSEFORGE -> Color(0xFFF16436)
        ModProvider.GITHUB -> Color(0xFF4078C0)
        ModProvider.URL -> PackVulcanTheme.colors.linkColor
        ModProvider.FILESYSTEM -> MaterialTheme.colors.onSurface
    }
}
