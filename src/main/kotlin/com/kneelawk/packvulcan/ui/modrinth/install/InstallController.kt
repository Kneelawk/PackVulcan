package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.kneelawk.packvulcan.engine.modrinth.DependencyCollector
import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import com.kneelawk.packvulcan.engine.modrinth.ProjectAndVersion
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.InstallOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

@Composable
fun rememberInstallController(
    display: InstallDisplay, acceptableVersions: AcceptableVersions, installedProjects: Set<String>,
    install: (InstallOperation) -> Unit, onLoading: (loading: Boolean) -> Unit, installScope: CoroutineScope,
    autoInstall: Boolean, beforeDialog: suspend () -> Unit, shape: Shape, modifier: Modifier, enabled: Boolean,
    content: @Composable RowScope.() -> Unit
): InstallInterface {
    val log = remember { KotlinLogging.logger { } }

    val loadingState = remember { mutableStateOf(false) }
    val loadingTextState = remember { mutableStateOf("") }
    val dialogOpenState = remember { mutableStateOf(false) }

    val pavState = remember { mutableStateOf<ProjectAndVersion?>(null) }

    val modNameState = remember { derivedStateOf { pavState.value?.project?.title ?: "" } }
    val modVersionState = remember { derivedStateOf { pavState.value?.version?.versionNumber ?: "" } }

    val collectedDependencies = remember { mutableStateListOf<DependencyDisplay>() }

    fun setLoading(loading: Boolean) {
        onLoading(loading)
        loadingState.value = loading
    }

    suspend fun doInstall() {
        setLoading(true)
        loadingTextState.value = "Getting Mod..."

        val project = ModrinthApi.project(display.projectId).escapeIfRight {
            log.warn("Error loading project: ${it.id}")
            loadingTextState.value = "Project Error."
            setLoading(false)
            return
        }

        val version = when (display.version) {
            InstallVersion.Latest -> ModrinthUtils.latestVersion(display.projectId, acceptableVersions)
            is InstallVersion.Specific -> ModrinthApi.version(display.version.versionId).leftOrNull()
        } ?: run {
            log.warn("Error loading ${project.title} version: ${display.version}")
            loadingTextState.value = "Version Error."
            setLoading(false)
            return
        }

        val pav = ProjectAndVersion(project, version)
        pavState.value = pav

        loadingTextState.value = "Getting Deps..."

        val deps = DependencyCollector.collectDependencies(version.dependencies, acceptableVersions, installedProjects)
        collectedDependencies.clear()
        collectedDependencies.addAll(deps.asSequence().map {
            DependencyDisplay(it, mutableStateOf(true))
        })

        if (autoInstall && deps.isEmpty()) {
            install(InstallOperation(listOf(pav.toModInfo())))

            loadingTextState.value = "Done."
            setLoading(false)
        } else {
            loadingTextState.value = "Installing..."

            beforeDialog()
            dialogOpenState.value = true
        }
    }

    return remember(installedProjects, enabled, shape, modifier, content) {
        object : InstallInterface {
            override val enabled = enabled
            override val loading by loadingState
            override val loadingText by loadingTextState
            override val buttonShape = shape
            override val buttonModifier = modifier
            override val content = content
            override val dialogOpen by dialogOpenState
            override val modName by modNameState
            override val modVersion by modVersionState
            override val collectedDependencies = collectedDependencies

            override fun startInstall() {
                if (enabled && !installedProjects.contains(display.projectId)) {
                    installScope.launch {
                        doInstall()
                    }
                }
            }

            override fun cancelDialog() {
                dialogOpenState.value = false
                setLoading(false)
            }

            override fun install() {
                installScope.launch {
                    val mod = pavState.value?.toModInfo() ?: return@launch
                    val toInstall = (collectedDependencies.asSequence().filter { it.install.value }
                        .map { it.mod } + mod).toList()

                    install(InstallOperation(toInstall))

                    dialogOpenState.value = false
                    loadingTextState.value = "Done."
                    setLoading(false)
                }
            }
        }
    }
}
