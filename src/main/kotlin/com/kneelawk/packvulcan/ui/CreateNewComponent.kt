package com.kneelawk.packvulcan.ui

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.packvulcan.GlobalConstants.INITIAL_LOADER_VERSION
import com.kneelawk.packvulcan.GlobalConstants.INITIAL_MINECRAFT_VERSION
import com.kneelawk.packvulcan.GlobalConstants.INITIAL_PROJECT_VERSION
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.util.ComponentScope
import com.kneelawk.packvulcan.util.Conflator
import com.kneelawk.packvulcan.util.PathUtils
import com.kneelawk.packvulcan.util.VersionUtils
import kotlinx.coroutines.launch
import java.nio.file.Paths

class CreateNewComponent(context: ComponentContext, private val finish: (CreateNewResult) -> Unit) :
    ComponentContext by context {
    private val scope = ComponentScope(lifecycle)

    var location by mutableStateOf("")
    var name by mutableStateOf("")
    var author by mutableStateOf("")

    var version by mutableStateOf(INITIAL_PROJECT_VERSION)
    val versionError by derivedStateOf { !VersionUtils.isSemVer(version) }

    var editMinecraftVersion by mutableStateOf(INITIAL_MINECRAFT_VERSION)
        private set
    private var minecraftVersionState by mutableStateOf(MinecraftVersionState(null, ""))
    private var minecraftVersionLoading by mutableStateOf(false)
    private val minecraftVersionConflator = Conflator<String>(scope) { version ->
        minecraftVersionLoading = true
        minecraftVersionState = MinecraftVersion.forVersion(version).switch(
            { MinecraftVersionState(it, "") },
            { MinecraftVersionState(null, it.toString()) }
        )
        minecraftVersionLoading = false
    }
    private val minecraftVersion by derivedStateOf { minecraftVersionState.version }
    val minecraftVersionError by derivedStateOf { minecraftVersionState.error }

    var editLoaderVersion by mutableStateOf(INITIAL_LOADER_VERSION)
        private set
    private var loaderVersionState by mutableStateOf(LoaderVersionState(null, ""))
    private var loaderVersionLoading by mutableStateOf(false)
    private val loaderVersionConflator = Conflator<LoaderVersionInput>(scope) { input ->
        loaderVersionLoading = true
        val version = input.loaderVersion
        loaderVersionState = LoaderVersion.forVersion(version, input.minecraftVersion).switch(
            { LoaderVersionState(it, "") },
            { LoaderVersionState(null, it.toString()) }
        )
        loaderVersionLoading = false
    }
    private val loaderVersion by derivedStateOf { loaderVersionState.version }
    val loaderVersionError by derivedStateOf { loaderVersionState.error }

    var additionalMinecraftText by mutableStateOf("")
    val minecraftVersions = mutableStateListOf<String>()
    val additionalLoaders by derivedStateOf { LoaderVersion.Type.values().filter { it != loaderVersion?.type } }
    var acceptableVersions by mutableStateOf(AcceptableVersions())
    var acceptableVersionsLoading by mutableStateOf(false)

    val createEnabled by derivedStateOf {
        PathUtils.isPathValid(location)
                && name.isNotBlank()
                && author.isNotBlank()
                && VersionUtils.isSemVer(version)
                && minecraftVersion != null
                && loaderVersion != null
    }

    val showLoadingIcon by derivedStateOf { minecraftVersionLoading || loaderVersionLoading || acceptableVersionsLoading }

    init {
        scope.launch {
            acceptableVersionsLoading = true
            minecraftVersions.addAll(
                MinecraftVersion.minecraftVersionList()
                    .asSequence()
                    .map { it.version })
            acceptableVersionsLoading = false
        }
        minecraftVersionConflator.send(INITIAL_MINECRAFT_VERSION)
        loaderVersionConflator.send(LoaderVersionInput(INITIAL_LOADER_VERSION, INITIAL_MINECRAFT_VERSION))
    }

    fun setMinecraftVersion(version: String) {
        editMinecraftVersion = version
        minecraftVersionConflator.send(version)
        loaderVersionConflator.send(LoaderVersionInput(editLoaderVersion, version))
    }

    fun setLoaderVersion(version: String) {
        editLoaderVersion = version
        loaderVersionConflator.send(LoaderVersionInput(version, editMinecraftVersion))
    }

    fun addAdditionalMinecraft(version: String) {
        additionalMinecraftText = ""
        acceptableVersions = acceptableVersions.copy(minecraft = acceptableVersions.minecraft + version)
    }

    fun removeAdditionalMinecraft(version: String) {
        acceptableVersions = acceptableVersions.copy(minecraft = acceptableVersions.minecraft - version)
    }

    fun toggleAdditionalLoader(type: LoaderVersion.Type) {
        val newLoaders = if (acceptableVersions.loaders.contains(type.packwizName)) {
            acceptableVersions.loaders - type.packwizName
        } else {
            acceptableVersions.loaders + type.packwizName
        }

        acceptableVersions = acceptableVersions.copy(loaders = newLoaders)
    }

    fun cancel() {
        finish(CreateNewResult.Cancel)
    }

    fun create() {
        val location = location
        val name = name
        val author = author
        val version = version
        val minecraftVersion = minecraftVersion
        val loaderVersion = loaderVersion
        val acceptableVersions = acceptableVersions

        if (PathUtils.isPathValid(location)
            && name.isNotBlank()
            && author.isNotBlank()
            && VersionUtils.isSemVer(version)
            && minecraftVersion != null
            && loaderVersion != null
        ) {
            finish(
                CreateNewResult.Create(
                    NewModpack(
                        Paths.get(location), name, author, version, minecraftVersion, loaderVersion, acceptableVersions
                    )
                )
            )
        }
    }

    private data class MinecraftVersionState(val version: MinecraftVersion?, val error: String)
    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
    private data class LoaderVersionState(val version: LoaderVersion?, val error: String)
}

sealed class CreateNewResult {
    object Cancel : CreateNewResult()
    data class Create(val modpack: NewModpack) : CreateNewResult()
}
