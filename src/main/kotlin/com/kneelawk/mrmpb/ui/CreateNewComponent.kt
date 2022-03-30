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
    private var minecraftVersionState by mutableStateOf(MinecraftVersionState(INITIAL_MINECRAFT_VERSION, null, ""))
    private val minecraftVersionConflator = Conflator<String>(scope) { version ->
        minecraftVersionState = MinecraftVersion.forVersion(version).switch(
            { MinecraftVersionState(version, it, "") },
            { MinecraftVersionState(version, null, it.toString()) }
        )
    }
    private val minecraftVersion by derivedStateOf { minecraftVersionState.version }
    private val minecraftVersionValid by derivedStateOf { minecraftVersionState.valid }
    val minecraftVersionError by derivedStateOf { minecraftVersionState.error }

    var editLoaderVersion by mutableStateOf(INITIAL_LOADER_VERSION)
        private set
    private var loaderVersionState by mutableStateOf(LoaderVersionState(INITIAL_LOADER_VERSION, null, ""))
    private val loaderVersionConflator = Conflator<LoaderVersionInput>(scope) { input ->
        val version = input.loaderVersion
        loaderVersionState = LoaderVersion.forVersion(version, input.minecraftVersion).switch(
            { LoaderVersionState(version, it, "") },
            { LoaderVersionState(version, null, it.toString()) }
        )
    }
    private val loaderVersion by derivedStateOf { loaderVersionState.version }
    private val loaderVersionValid by derivedStateOf { loaderVersionState.valid }
    val loaderVersionError by derivedStateOf { loaderVersionState.error }

    val createEnabled by derivedStateOf {
        PathUtils.isPathValid(location)
                && name.isNotBlank()
                && author.isNotBlank()
                && VersionUtils.isSemVer(version)
                && minecraftVersionValid != null
                && loaderVersionValid != null
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
        val location = location
        val name = name
        val author = author
        val version = version
        val minecraftVersion = minecraftVersionValid
        val loaderVersion = loaderVersionValid

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

    private data class MinecraftVersionState(val version: String, val valid: MinecraftVersion?, val error: String)
    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
    private data class LoaderVersionState(val version: String, val valid: LoaderVersion?, val error: String)
}

sealed class CreateNewResult {
    object Cancel : CreateNewResult()
    data class Create(val modpack: NewModpack) : CreateNewResult()
}
