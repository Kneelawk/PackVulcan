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
import com.kneelawk.mrmpb.util.ComponentScope
import com.kneelawk.mrmpb.util.Conflator
import com.kneelawk.mrmpb.util.VersionUtils

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
    private var minecraftVersionState by mutableStateOf(MinecraftVersionState(INITIAL_MINECRAFT_VERSION, false))
    private val minecraftVersionConflator = Conflator<String>(scope) {
        val version = it
        minecraftVersionState = MinecraftVersion.forVersion(version).switch(
            { MinecraftVersionState(version, false) },
            { MinecraftVersionState(version, true) }
        )
    }
    val minecraftVersion by derivedStateOf { minecraftVersionState.version }
    val minecraftVersionError by derivedStateOf { minecraftVersionState.error }

    var editLoaderVersion by mutableStateOf(INITIAL_LOADER_VERSION)
        private set
    private var loaderVersionState by mutableStateOf(LoaderVersionState(INITIAL_LOADER_VERSION, false))
    private val loaderVersionConflator = Conflator<LoaderVersionInput>(scope) {
        val version = it.loaderVersion
        loaderVersionState = LoaderVersion.forVersion(version, it.minecraftVersion).switch(
            { LoaderVersionState(version, false) },
            { LoaderVersionState(version, true) }
        )
    }
    val loaderVersion by derivedStateOf { loaderVersionState.version }
    val loaderVersionError by derivedStateOf { loaderVersionState.error }

    val createEnabled by derivedStateOf {
        location.isNotBlank()
                && name.isNotBlank()
                && author.isNotBlank()
                && VersionUtils.isSemVer(version)
                && !minecraftVersionError
                && !loaderVersionError
    }

    fun setMinecraftVersion(version: String) {
        editMinecraftVersion = version
        minecraftVersionConflator.send(version)
        loaderVersionConflator.send(LoaderVersionInput(loaderVersion, version))
    }

    fun setLoaderVersion(version: String) {
        editLoaderVersion = version
        loaderVersionConflator.send(LoaderVersionInput(version, minecraftVersion))
    }

    fun cancel() {
        finish(CreateNewResult.Cancel)
    }

    fun create() {
    }

    private data class MinecraftVersionState(val version: String, val error: Boolean)
    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
    private data class LoaderVersionState(val version: String, val error: Boolean)

    private data class VersionState(val minecraftVersion: String, val loaderVersion: String)
}

sealed class CreateNewResult {
    object Cancel : CreateNewResult()
}
