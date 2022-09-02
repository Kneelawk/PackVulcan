package com.kneelawk.packvulcan.ui.modrinth.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.kneelawk.packvulcan.net.image.ImageResource
import com.kneelawk.packvulcan.ui.modrinth.install.InstallView
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.theme.PackVulcanTheme
import com.kneelawk.packvulcan.ui.util.ModIconWrapper
import com.kneelawk.packvulcan.ui.util.widgets.ModIcon
import com.kneelawk.packvulcan.ui.util.widgets.SmallButton
import com.kneelawk.packvulcan.util.LoadingState
import com.kneelawk.packvulcan.util.formatHumanReadable
import com.kneelawk.packvulcan.util.formatRelative
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchHitView(controller: ModrinthSearchInterface, searchHit: SearchHitDisplay) {
    val scope = rememberCoroutineScope()

    var modImage by remember { mutableStateOf<LoadingState<ModIconWrapper>>(LoadingState.Loading) }

    suspend fun loadModImage() {
        modImage = if (searchHit.iconUrl.isNullOrBlank()) {
            LoadingState.Loaded(ModIconWrapper.icon(PackVulcanIcons.noImage))
        } else {
            ImageResource.getModIcon(searchHit.iconUrl)?.let { LoadingState.Loaded(ModIconWrapper.image(it)) }
                ?: LoadingState.Error
        }
    }

    LaunchedEffect(Unit) {
        loadModImage()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { controller.openProject(searchHit) }
    ) {
        Row(
            modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            ModIcon(modImage) { scope.launch { loadModImage() } }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        searchHit.title, style = MaterialTheme.typography.h6,
                        color = PackVulcanTheme.colors.headingColor,
                        modifier = Modifier.weight(1f)
                    )

                    Text("by ${searchHit.author}")
                }

                Text(searchHit.description)

                FlowRow(mainAxisSpacing = 10.dp, crossAxisSpacing = 10.dp) {
                    for (category in searchHit.categories) {
                        key(category.apiName) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                category.icon?.icon(category.prettyName, androidx.compose.ui.Modifier.size(18.dp))
                                Text(category.prettyName)
                            }
                        }
                    }

                    for (loader in searchHit.loaders) {
                        key(loader.apiName) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                loader.icon?.icon(loader.prettyName, androidx.compose.ui.Modifier.size(18.dp))
                                Text(loader.prettyName)
                            }
                        }
                    }
                }

                FlowRow(mainAxisSpacing = 20.dp, crossAxisSpacing = 10.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Create, "created", modifier = androidx.compose.ui.Modifier.size(18.dp))
                        Text("Created ${searchHit.dateCreated.formatRelative()}")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, "updated", modifier = androidx.compose.ui.Modifier.size(18.dp))
                        Text("Updated ${searchHit.dateModified.formatRelative()}")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(PackVulcanIcons.download, "downloads")
                    Text(searchHit.downloads.formatHumanReadable(), fontWeight = FontWeight.Bold)
                    Text("downloads")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Favorite, "follows")
                    Text(searchHit.follows.formatHumanReadable(), fontWeight = FontWeight.Bold)
                    Text("followers")
                }

                Column(modifier = Modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val installed = controller.isModInstalled(searchHit)
                        SmallButton(
                            onClick = { controller.installLatest(searchHit) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small.copy(
                                bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)
                            ),
                            enabled = searchHit.compatible && !installed
                        ) {
                            if (installed) {
                                Icon(Icons.Default.Check, "installed")
                                Text("Already Installed", modifier = Modifier.padding(start = 5.dp))
                            } else {
                                if (searchHit.compatible) {
                                    Icon(PackVulcanIcons.download, "install")
                                    Text("Install Latest...", modifier = Modifier.padding(start = 5.dp))
                                } else {
                                    Icon(Icons.Default.Close, "incompatible")
                                    Text("Incompatible", modifier = Modifier.padding(start = 5.dp))
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = searchHit.id == controller.installLatest?.projectId,
                            onDismissRequest = controller::cancelInstallLatest
                        ) {
                            controller.installLatest?.let {
                                InstallView(
                                    display = it,
                                    acceptableVersions = controller.acceptableVersions,
                                    installedProjects = controller.installedProjects,
                                    onCloseRequest = controller::cancelInstallLatest,
                                    install = controller::install
                                )
                            }
                        }
                    }

                    SmallButton(
                        onClick = { controller.browseVersions(searchHit) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small.copy(
                            topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
                        )
                    ) {
                        Icon(Icons.Default.List, "browse")
                        Text("Browse Versions...", modifier = Modifier.padding(start = 5.dp))
                    }
                }
            }
        }
    }
}
