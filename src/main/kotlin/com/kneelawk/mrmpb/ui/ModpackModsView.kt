package com.kneelawk.mrmpb.ui

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.kneelawk.mrmpb.engine.image.ImageUtils
import com.kneelawk.mrmpb.engine.modinfo.ModInfo
import com.kneelawk.mrmpb.engine.packwiz.PackwizMetaFile
import com.kneelawk.mrmpb.model.FullModInfo
import com.kneelawk.mrmpb.model.ModIcon
import com.kneelawk.mrmpb.net.image.ImageResource
import com.kneelawk.mrmpb.ui.theme.MrMpBIcons
import com.kneelawk.mrmpb.ui.theme.MrMpBTheme
import com.kneelawk.mrmpb.ui.util.ImageWrapper
import com.kneelawk.mrmpb.ui.util.layout.VerticalScrollWrapper
import com.kneelawk.mrmpb.util.LoadingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

@Composable
fun ModpackModsView(component: ModpackComponent) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth().shadow(5.dp, RectangleShape, true)
                .background(MaterialTheme.colors.surface)
        ) {
            Row(modifier = Modifier.padding(20.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Add Mods...")
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
                items(component.modsList) { mod ->
                    ModpackModView(component, mod)
                }
            }
        }
    }
}

@Composable
fun ModpackModView(component: ModpackComponent, mod: PackwizMetaFile) {
    val scope = rememberCoroutineScope()

    var modInfo by remember { mutableStateOf<LoadingState<FullModInfo>>(LoadingState.Loading) }

    suspend fun loadModInfo() {
        modInfo = supervisorScope {
            try {
                // getting null here means mod data couldn't be loaded
                ModInfo.getFullInfo(mod)?.let { LoadingState.Loaded(it) } ?: LoadingState.Error
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

    suspend fun loadModIcon(modInfo: LoadingState<FullModInfo>) {
        modImage = when (modInfo) {
            is LoadingState.Loaded -> LoadingState.Loaded(
                when (val icon = modInfo.data.icon) {
                    is ModIcon.Buffered -> ImageWrapper.ImageBitmap(icon.image.toComposeImageBitmap())
                    is ModIcon.Url -> ImageResource.getModIcon(icon.url)?.let { ImageWrapper.ImageBitmap(it) }
                        ?: ImageWrapper.Painter(MrMpBIcons.noImage)
                    null -> ImageWrapper.Painter(MrMpBIcons.noImage)
                }
            )
            LoadingState.Error -> LoadingState.Loaded(ImageWrapper.Painter(MrMpBIcons.error))
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
                    Text(mod.toml.name, style = MaterialTheme.typography.h6, color = MrMpBTheme.colors.headingColor)

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
        }
    }
}
