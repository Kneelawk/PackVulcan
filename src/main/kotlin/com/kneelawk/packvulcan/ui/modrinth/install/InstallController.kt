package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.runtime.*
import com.kneelawk.packvulcan.engine.modrinth.DependencyCollector
import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import com.kneelawk.packvulcan.engine.modrinth.ProjectAndVersion
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.InstallOperation
import com.kneelawk.packvulcan.util.MSet
import kotlinx.coroutines.launch

@Composable
fun rememberInstallController(
    display: InstallDisplay, acceptableVersions: AcceptableVersions, installedProjects: MSet<String>,
    onCloseRequest: () -> Unit, install: (InstallOperation) -> Unit, autoInstall: Boolean = true
): InstallInterface {
    val scope = rememberCoroutineScope()

    val loadingState = remember { mutableStateOf(true) }
    val loadingTextState = remember { mutableStateOf("") }

    val pavState = remember { mutableStateOf<ProjectAndVersion?>(null) }

    val modNameState = remember { derivedStateOf { pavState.value?.project?.title ?: "" } }
    val modVersionState = remember { derivedStateOf { pavState.value?.version?.versionNumber ?: "" } }

    val collectedDependencies = remember { mutableStateListOf<DependencyDisplay>() }

    LaunchedEffect(display, acceptableVersions, installedProjects) {
        loadingState.value = true
        loadingTextState.value = "Getting mod info..."

        val project = ModrinthApi.project(display.projectId).escapeIfRight {
            loadingTextState.value = "Error loading project."
            return@LaunchedEffect
        }

        val version = when (display.version) {
            InstallVersion.Latest -> ModrinthUtils.latestVersion(display.projectId, acceptableVersions)
            is InstallVersion.Specific -> ModrinthApi.version(display.version.versionId).leftOrNull()
        } ?: run {
            loadingTextState.value = "Error loading version."
            return@LaunchedEffect
        }

        val pav = ProjectAndVersion(project, version)
        pavState.value = pav

        loadingTextState.value = "Collecting dependencies..."

        val deps = DependencyCollector.collectDependencies(version.dependencies, acceptableVersions, installedProjects)
        collectedDependencies.clear()
        collectedDependencies.addAll(deps.asSequence().map {
            DependencyDisplay(it, mutableStateOf(true))
        })

        if (autoInstall && deps.isEmpty()) {
            install(InstallOperation(listOf(pav.toModInfo())))
        }

        loadingTextState.value = "Done."
        loadingState.value = false
    }

    return remember {
        object : InstallInterface {
            override val loading by loadingState
            override val loadingText by loadingTextState
            override val modName by modNameState
            override val modVersion by modVersionState
            override val collectedDependencies = collectedDependencies

            override fun cancel() {
                onCloseRequest()
            }

            override fun install() {
                scope.launch {
                    val mod = pavState.value?.toModInfo() ?: return@launch
                    val toInstall = (collectedDependencies.asSequence().filter { it.install.value }
                        .map { it.mod } + mod).toList()

                    install(InstallOperation(toInstall))
                }
            }
        }
    }
}
