package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.mrmpb.GlobalConstants.INITIAL_LOADER_VERSION
import com.kneelawk.mrmpb.GlobalConstants.INITIAL_MINECRAFT_VERSION
import com.kneelawk.mrmpb.GlobalConstants.INITIAL_PROJECT_VERSION
import com.kneelawk.mrmpb.model.LoaderVersion
import com.kneelawk.mrmpb.model.MinecraftVersion
import com.kneelawk.mrmpb.model.NewModpack
import com.kneelawk.mrmpb.util.ComponentScope
import com.kneelawk.mrmpb.util.Conflator
import com.kneelawk.mrmpb.util.PathUtils
import com.kneelawk.mrmpb.util.VersionUtils
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

    val createEnabled by derivedStateOf {
        PathUtils.isPathValid(location)
                && name.isNotBlank()
                && author.isNotBlank()
                && VersionUtils.isSemVer(version)
                && minecraftVersion != null
                && loaderVersion != null
    }

    val showLoadingIcon by derivedStateOf { minecraftVersionLoading || loaderVersionLoading }

    init {
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

        if (PathUtils.isPathValid(location)
            && name.isNotBlank()
            && author.isNotBlank()
            && VersionUtils.isSemVer(version)
            && minecraftVersion != null
            && loaderVersion != null
        ) {
            finish(
                CreateNewResult.Create(
                    NewModpack(Paths.get(location), name, author, version, minecraftVersion, loaderVersion)
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
