package com.kneelawk.packvulcan.ui

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.packvulcan.GlobalConstants.HOME_FOLDER
import com.kneelawk.packvulcan.engine.modinfo.ModFileInfo
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.engine.packwiz.PackwizModFile
import com.kneelawk.packvulcan.engine.packwiz.PackwizProject
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.ui.instance.InstanceManager
import com.kneelawk.packvulcan.util.ComponentScope
import com.kneelawk.packvulcan.util.Conflator
import com.kneelawk.packvulcan.util.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.relativeTo

class ModpackComponent(context: ComponentContext, args: ModpackComponentArgs) : ComponentContext by context {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    /*
     * Component Stuff.
     */

    private val scope = ComponentScope(lifecycle)

    /*
     * View Stuff.
     */

    var loading by mutableStateOf(true)
    var openDialogOpen by mutableStateOf(false)

    /*
     * Details Stuff.
     */

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

    /*
     * Mod List Stuff.
     */

    val modsList = mutableStateListOf<PackwizMod>()
    private val modsMap = mutableMapOf<String, PackwizMod>()

    private var modsDir = Paths.get("")
    var previousSelectionDir = HOME_FOLDER

    /*
     * Mod Adding Stuff.
     */

    var modrinthSearchDialogOpen by mutableStateOf(false)
    val selectedMinecraftVersions = mutableStateMapOf<String, Unit>()
    val selectedModLoaders = mutableStateMapOf<LoaderVersion.Type, Unit>()

    /*
     * Attribution Stuff.
     */

    var attributorDialogOpen by mutableStateOf(false)

    init {
        scope.launch {
            val project = when (args) {
                is ModpackComponentArgs.CreateNew -> {
                    log.info("Creating new packwiz project at '${args.newModpack.location}'...")
                    PackwizProject.createNew(args.newModpack)
                }

                is ModpackComponentArgs.OpenExisting -> {
                    val projectDir = args.packFile.parent
                    log.info("Loading existing packwiz project at '${projectDir}'...")
                    PackwizProject.loadFromProjectDir(projectDir)
                }
            }

            loadFromProject(project)

            loading = false
        }
    }

    private suspend fun loadFromProject(project: PackwizProject) {
        // are these refreshes all over the place a good idea?
        project.refresh()

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

        modsDir = project.modsDir

        setModsList(project.getMods())

        log.info("Writing packwiz project to '${project.projectDir}'...")
        project.write()
    }

    fun reload() {
        if (!loading) {
            loading = true

            scope.launch {
                log.info("Loading existing packwiz project at '${modpackLocation}'...")
                val project = PackwizProject.loadFromProjectDir(modpackLocation)

                loadFromProject(project)

                loading = false
            }
        }
    }

    private fun setModsList(mods: List<PackwizMod>) {
        modsList.clear()
        modsList.addAll(mods.sortedBy { it.filePath })
        modsMap.clear()
        modsMap.putAll(mods.associateBy { it.filePath })
    }

    private fun addOrReplaceModInList(mod: PackwizMod) {
        if (modsMap.containsKey(mod.filePath)) {
            val index = modsList.indexOfFirst { it.filePath == mod.filePath }
            modsList[index] = mod
        } else {
            val index = modsList.indexOfFirst { it.filePath > mod.filePath }
            modsList.add(index, mod)
        }

        modsMap[mod.filePath] = mod
    }

    fun removeMod(filePath: String) {
        modsList.removeAll { it.filePath == filePath }
        modsMap.remove(filePath)
    }

    fun modFilenameConflicts(path: Path): Boolean {
        val newModFile = modsDir.resolve(path.name)
        val relative = newModFile.relativeTo(modpackLocation).invariantSeparatorsPathString
        return modsMap.containsKey(relative)
    }

    fun addModJar(path: Path) {
        previousSelectionDir = path.parent

        if (!loading) {
            loading = true

            scope.launch {
                // copy file to mods dir
                val newModFile = modsDir.resolve(path.name)

                withContext(Dispatchers.IO) {
                    path.copyTo(newModFile, overwrite = true)
                }

                if (path.name.endsWith(".jar")) {
                    val info = ModFileInfo.getFileInfo(newModFile)
                    if (info != null) {
                        val relative = newModFile.relativeTo(modpackLocation).invariantSeparatorsPathString

                        addOrReplaceModInList(PackwizModFile(relative, null, false, newModFile, info))
                    }
                }

                loading = false
            }
        }
    }

    fun save() {
        if (!loading) {
            loading = true

            scope.launch {
                log.info("Updating existing packwiz project at '$modpackLocation'...")
                val project = PackwizProject.loadFromProjectDir(modpackLocation)

                // are these refreshes all over the place a good idea?
                project.refresh()

                project.pack = project.pack.copy(
                    name = modpackName,
                    author = modpackAuthor,
                    version = modpackVersion,
                    versions = project.pack.versions.copy(
                        minecraft = minecraftVersion.version,
                        loaderVersions = mapOf(loaderVersion.type.packwizName to loaderVersion.version)
                    )
                )

                project.setMods(modsList)

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

    fun new() {
        InstanceManager.newRoot(RootInitialState.CreateNew)
    }

    fun open() {
        openDialogOpen = true
    }

    fun open(path: Path) {
        InstanceManager.newRoot(RootInitialState.Open(path))
    }

    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
}

sealed class ModpackComponentArgs {
    data class CreateNew(val newModpack: NewModpack) : ModpackComponentArgs()
    data class OpenExisting(val packFile: Path) : ModpackComponentArgs()
}
