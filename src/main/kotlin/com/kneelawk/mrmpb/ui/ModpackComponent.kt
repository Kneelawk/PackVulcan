package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.mrmpb.engine.packwiz.PackwizProject
import com.kneelawk.mrmpb.model.LoaderVersion
import com.kneelawk.mrmpb.model.MinecraftVersion
import com.kneelawk.mrmpb.model.NewModpack
import com.kneelawk.mrmpb.util.ComponentScope
import com.kneelawk.mrmpb.util.Conflator
import com.kneelawk.mrmpb.util.VersionUtils
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.nio.file.Path
import java.nio.file.Paths

class ModpackComponent(context: ComponentContext, args: ModpackComponentArgs) : ComponentContext by context {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val scope = ComponentScope(lifecycle)

    var loading by mutableStateOf(true)

    var modpackLocation: Path by mutableStateOf(Paths.get(""))
        private set
    var modpackName by mutableStateOf("")
    var modpackAuthor by mutableStateOf("")

    var editModpackVersion by mutableStateOf("")
        private set
    var modpackVersion by mutableStateOf("0.1.0")
        private set
    var modpackVersionError by mutableStateOf(false)

    var editMinecraftVersion by mutableStateOf("")
        private set
    private var minecraftVersion by mutableStateOf(MinecraftVersion.DEFAULT_VERSION)
    var minecraftVersionError by mutableStateOf("")
        private set
    private var minecraftVersionLoading by mutableStateOf(false)
    private val minecraftVersionConflator = Conflator<String>(scope) { version ->
        minecraftVersionLoading = true
        MinecraftVersion.forVersion(version).switch(
            {
                minecraftVersionError = ""
                minecraftVersion = it
            },
            { minecraftVersionError = "$it The version '$minecraftVersion' will be used instead." }
        )
        minecraftVersionLoading = false
    }

    var editLoaderVersion by mutableStateOf("")
        private set
    private var loaderVersion by mutableStateOf(LoaderVersion.DEFAULT_VERSION)
    var loaderVersionError by mutableStateOf("")
        private set
    private var loaderVersionLoading by mutableStateOf(false)
    private val loaderVersionConflator = Conflator<LoaderVersionInput>(scope) { version ->
        loaderVersionLoading = true
        LoaderVersion.forVersion(version.loaderVersion, version.minecraftVersion).switch(
            {
                loaderVersionError = ""
                loaderVersion = it
            },
            { loaderVersionError = "$it The version '$loaderVersion' will be used instead." }
        )
        loaderVersionLoading = false
    }

    val showLoadingIcon by derivedStateOf { minecraftVersionLoading || loaderVersionLoading }

    init {
        scope.launch {
            val project = when (args) {
                is ModpackComponentArgs.CreateNew -> {
                    log.info("Creating new packwiz project at '${args.newModpack.location}'...")
                    PackwizProject.createNew(args.newModpack)
                }
                is ModpackComponentArgs.OpenExisting -> {
                    log.info("Loading existing packwiz project at '${args.packFile}'...")
                    PackwizProject.loadExisting(args.packFile)
                }
            }

            modpackLocation = project.projectDir
            modpackName = project.pack.name
            modpackAuthor = project.pack.author ?: ""
            updateModpackVersion(project.pack.version ?: "")

            val minecraftVersion = project.pack.versions.minecraft

            val loaderVersion = project.pack.versions.loaderVersions.entries.firstNotNullOfOrNull {
                LoaderVersion.Type.fromPackwizName(
                    it.key
                )?.to(it.value)
            }?.let {
                "${it.first.prettyName} ${it.second}"
            } ?: ""

            updateMinecraftAndLoaderVersions(loaderVersion, minecraftVersion)

            log.info("Writing packwiz project to '${project.projectDir}'...")
            project.write()

            loading = false
        }
    }

    fun save() {
        if (!loading) {
            loading = true

            scope.launch {
                val packFile = PackwizProject.getPackFile(modpackLocation)
                log.info("Updating existing packwiz project at '$packFile'...")

                val project = PackwizProject.loadExisting(packFile)

                project.pack = project.pack.copy(
                    name = modpackName,
                    author = modpackAuthor,
                    version = modpackVersion,
                    versions = project.pack.versions.copy(
                        minecraft = minecraftVersion.version,
                        loaderVersions = mapOf(loaderVersion.type.packwizName to loaderVersion.version)
                    )
                )

                log.info("Writing packwiz project at '${project.projectDir}'...")
                project.write()

                loading = false
            }
        }
    }

    fun updateModpackVersion(version: String) {
        editModpackVersion = version
        // update the valid version if it's valid
        if (VersionUtils.isSemVer(version)) {
            modpackVersion = version
            modpackVersionError = false
        } else {
            modpackVersionError = true
        }
    }

    private fun updateMinecraftAndLoaderVersions(loaderVersion: String, minecraftVersion: String) {
        editMinecraftVersion = minecraftVersion
        editLoaderVersion = loaderVersion
        minecraftVersionConflator.send(minecraftVersion)
        loaderVersionConflator.send(LoaderVersionInput(loaderVersion, minecraftVersion))
    }

    fun updateMinecraftVersion(version: String) {
        editMinecraftVersion = version
        minecraftVersionConflator.send(version)
        loaderVersionConflator.send(LoaderVersionInput(editLoaderVersion, version))
    }

    fun updateLoaderVersion(version: String) {
        editLoaderVersion = version
        loaderVersionConflator.send(LoaderVersionInput(version, editMinecraftVersion))
    }

    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
}

sealed class ModpackComponentArgs {
    data class CreateNew(val newModpack: NewModpack) : ModpackComponentArgs()
    data class OpenExisting(val packFile: Path) : ModpackComponentArgs()
}
