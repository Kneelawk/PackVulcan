package com.kneelawk.packvulcan.ui

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.engine.modinfo.ModFileInfo
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.model.ModIcon
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.ui.util.dialog.file.OpenFileDialog
import com.kneelawk.packvulcan.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private val log = KotlinLogging.logger { }

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

                Button(onClick = {}, modifier = Modifier.weight(1f), shape = startShape, enabled = !component.loading) {
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
            backgroundColor = Color.Transparent, backgroundShape = MaterialTheme.shapes.large, scrollbarPadding = 0.dp
        ) {
            LazyColumn(
                state = lazyListState, modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(15.dp), contentPadding = PaddingValues(20.dp)
            ) {
                items(component.modsList, key = { it.filePath }) { mod ->
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

    var modImage by remember { mutableStateOf<LoadingState<ImageWrapper>>(LoadingState.Loading) }

    suspend fun loadModIcon(modInfo: LoadingState<SimpleModInfo>) {
        modImage = when (modInfo) {
            is LoadingState.Loaded -> LoadingState.Loaded(
                when (val icon = modInfo.data.icon) {
                    is ModIcon.Buffered -> ImageWrapper.ImageBitmap(icon.image.toComposeImageBitmap())
                    is ModIcon.Url -> ImageResource.getModIcon(icon.url)?.let { ImageWrapper.ImageBitmap(it) }
                        ?: ImageWrapper.Painter(PackVulcanIcons.noImage)
                    null -> ImageWrapper.Painter(PackVulcanIcons.noImage)
                }
            )
            LoadingState.Error -> LoadingState.Loaded(ImageWrapper.Painter(PackVulcanIcons.error))
            LoadingState.Loading -> LoadingState.Loading
        }
    }

    LaunchedEffect(modInfo) {
        loadModIcon(modInfo)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(ImageUtils.MOD_ICON_SIZE.dp)) {
                when (val modImage = modImage) {
                    LoadingState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    LoadingState.Error -> {
                        IconButton(onClick = {
                            scope.launch {
                                loadModIcon(modInfo)
                            }
                        }, modifier = Modifier.align(Alignment.Center)) {
                            Icon(Icons.Default.Refresh, "reload image")
                        }
                    }
                    is LoadingState.Loaded -> {
                        modImage.data.iconOrBitmap(
                            "mod icon",
                            modifier = Modifier.align(Alignment.Center)
                                .size(ImageUtils.MOD_ICON_SIZE.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Text("${modInfo.data.versionName} - ${modInfo.data.filename}")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(onClick = {
                component.removeMod(mod.filePath)
            }, enabled = !component.loading) {
                Text("Remove")
            }
        }
    }
}
