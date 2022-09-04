package com.kneelawk.packvulcan.ui.modrinth.install

import androidx.compose.runtime.*
import com.kneelawk.packvulcan.engine.modrinth.install.InstallRequest
import com.kneelawk.packvulcan.engine.modrinth.install.ModInstaller
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.SimpleModInfo
import com.kneelawk.packvulcan.ui.InstallOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

@Composable
fun rememberSimpleInstallerState(
    acceptableVersions: AcceptableVersions, installedProjects: Set<String>, onInstall: (InstallOperation) -> Unit
): InstallerState {
    val scope = rememberCoroutineScope()
    val acceptableVersionsState = rememberUpdatedState(acceptableVersions)
    val installedProjectsState = rememberUpdatedState(installedProjects)
    val onInstallState = rememberUpdatedState(onInstall)

    return remember { SimpleInstallerState(scope, acceptableVersionsState, installedProjectsState, onInstallState) }
}

private class SimpleInstallerState(
    val scope: CoroutineScope, val acceptableVersions: State<AcceptableVersions>,
    val installedProjects: State<Set<String>>, val onInstall: State<(InstallOperation) -> Unit>
) : InstallerState {
    private val log = KotlinLogging.logger { }

    private var composed by mutableStateOf(emptySet<String>())
    private val installs = mutableStateMapOf<String, SimpleInstallState>()
    private val dialogQueue = mutableStateListOf<QueuedDialog>()

    private fun endInstall(request: InstallRequest) {
        // SnapshotStateMaps don't appear to handle removes properly, so we reset first so the UI resets,
        // then we remove the element.
        installs[request.projectId] = SimpleInstallState(request)
        scope.launch { installs.remove(request.projectId) }
    }

    override fun buttonCompose(request: InstallRequest) {
        composed += request.projectId
    }

    override fun buttonDecompose(request: InstallRequest) {
        composed -= request.projectId
    }

    override fun startInstall(request: InstallRequest) {
        installs.merge(
            request.projectId, SimpleInstallState(request, loading = true)
        ) { o, n -> o.copy(loading = n.loading) }

        scope.launch {
            val res = ModInstaller.doInstall(
                request = request, acceptableVersions = acceptableVersions.value,
                installedProjects = installedProjects.value, progressMsg = {
                    installs.merge(
                        request.projectId, SimpleInstallState(request, loadingMsg = it)
                    ) { o, n -> o.copy(loadingMsg = n.loadingMsg) }
                }, autoInstall = true
            )

            when (res) {
                is ModInstaller.InstallSingle -> {
                    onInstall.value(InstallOperation(listOf(res.mod)))
                    endInstall(request)
                }
                is ModInstaller.Dependencies -> {
                    dialogQueue.add(QueuedDialog(request, res.mod, res.dependencies))
                }
                is ModInstaller.ProjectError -> {
                    // uhhh I guess log it?
                    log.warn("Error loading project: ${res.projectId}")
                    endInstall(request)
                }
                is ModInstaller.VersionError -> {
                    log.warn("Error loading vesion of project ${res.project.title}: ${res.installVersion}")
                    endInstall(request)
                }
            }
        }
    }

    override fun loading(request: InstallRequest): Boolean {
        return installs[request.projectId]?.loading ?: false
    }

    override fun loadingMsg(request: InstallRequest): String {
        return installs[request.projectId]?.loadingMsg ?: ""
    }

    @Composable
    override fun showHiddenDependencyDialog() {
        val next = dialogQueue.firstOrNull() ?: return

        if (!composed.contains(next.request.projectId)) {
            DependenciesDialog(
                modName = next.mod.name,
                modVersion = next.mod.version,
                dependencies = next.dependencies,
                onCancel = {
                    dialogQueue.removeFirst()
                    endInstall(next.request)
                },
                onInstall = {
                    dialogQueue.removeFirst()
                    onInstall.value(InstallOperation(it + next.mod))
                    endInstall(next.request)
                }
            )
        }
    }

    @Composable
    override fun showButtonDependencyPopup(request: InstallRequest) {
        val next = dialogQueue.firstOrNull() ?: return
        val projectId = next.request.projectId

        // We shouldn't need to check composed here because this method shouldn't be called if the button is not
        // composed.
        DependenciesPopup(
            expanded = projectId == request.projectId,
            modName = next.mod.name,
            modVersion = next.mod.version,
            dependencies = next.dependencies,
            onCancel = {
                dialogQueue.removeFirstOrNull()
                endInstall(next.request)
            },
            onInstall = {
                dialogQueue.removeFirstOrNull()
                onInstall.value(InstallOperation(it + next.mod))
                endInstall(next.request)
            }
        )
    }
}

private data class SimpleInstallState(
    val request: InstallRequest, val loading: Boolean = false, val loadingMsg: String = ""
)

private data class QueuedDialog(
    val request: InstallRequest, val mod: SimpleModInfo, val dependencies: List<SimpleModInfo>
)
