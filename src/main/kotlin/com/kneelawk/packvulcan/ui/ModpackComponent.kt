package com.kneelawk.packvulcan.ui

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.packvulcan.GlobalConstants.HOME_FOLDER
import com.kneelawk.packvulcan.engine.modinfo.ModFileInfo
import com.kneelawk.packvulcan.engine.packwiz.PackwizMetaFile
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import com.kneelawk.packvulcan.engine.packwiz.PackwizProject
import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.model.packwiz.pack.OptionsToml
import com.kneelawk.packvulcan.ui.detail.DetailSelector
import com.kneelawk.packvulcan.ui.detail.ModrinthProjectSel
import com.kneelawk.packvulcan.ui.detail.PackwizFileSel
import com.kneelawk.packvulcan.ui.detail.ViewType
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

    var additionalMinecraftText by mutableStateOf("")
    val minecraftVersions = mutableStateListOf<String>()
    val additionalLoaders by derivedStateOf { LoaderVersion.Type.values().filter { it != loaderVersion.type } }
    var extraAcceptableVersions by mutableStateOf(AcceptableVersions())

    val acceptableVersions by derivedStateOf {
        extraAcceptableVersions.copy(
            minecraft = extraAcceptableVersions.minecraft + minecraftVersion.version,
            loaders = extraAcceptableVersions.loaders + loaderVersion.type.packwizName
        )
    }

    /*
     * Mod List Stuff.
     */

    val modsList = mutableStateListOf<PackwizMod>()
    private val modsMap = mutableMapOf<String, PackwizMod>()
    var modrinthProjects by mutableStateOf(emptySet<String>())

    private var modsDir = Paths.get("")
    private var modsPathStr = "mods/"
    private var metafileExtension = ".pw.toml"
    var previousSelectionDir = HOME_FOLDER

    /*
     * Mod Adding Stuff.
     */

    var modrinthSearchDialogOpen by mutableStateOf(false)
    val selectedMinecraftVersions = mutableStateMapOf<String, Unit>()
    val selectedModLoaders = mutableStateMapOf<LoaderVersion.Type, Unit>()

    /*
     * Mod Details Stuff.
     */

    var openProjectWindows by mutableStateOf(emptyMap<Any, DetailSelector>())

    /*
     * Attribution Stuff.
     */

    var attributorDialogOpen by mutableStateOf(false)

    init {
        scope.launch {
            minecraftVersions.addAll(
                MinecraftVersion.minecraftVersionList()
                    .asSequence()
                    .map { it.version })
        }
        scope.launch {
            val project = when (args) {
                is ModpackComponentArgs.CreateNew -> {
                    log.info("Creating new packwiz project at '${args.newModpack.location}'...")
                    val project = PackwizProject.createNew(args.newModpack)

                    // specifically when creating a project, we want it to index and create project files
                    project.refresh()
                    project.write()

                    project
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
        modsPathStr = project.modsPathStr
        metafileExtension = project.packFormat.metafileExtension

        extraAcceptableVersions = AcceptableVersions(
            project.pack.options?.acceptableGameVersions.orEmpty().toSet(),
            project.pack.options?.acceptableLoaders.orEmpty().toSet()
        )

        setModsList(project.getMods())
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
        modrinthProjects = mods.mapNotNull { (it as? PackwizMetaFile)?.toml?.update?.modrinth?.modId }.toSet()
    }

    private fun addOrReplaceModInList(mod: PackwizMod) {
        if (modsMap.containsKey(mod.filePath)) {
            val index = modsList.indexOfFirst { it.filePath == mod.filePath }
            modsList[index] = mod
        } else {
            val index = modsList.indexOfFirst { it.filePath > mod.filePath }
            if (index == -1) {
                modsList.add(mod)
            } else {
                modsList.add(index, mod)
            }
        }

        modsMap[mod.filePath] = mod

        (mod as? PackwizMetaFile)?.toml?.update?.modrinth?.modId?.let { modrinthProjects += it }
    }

    fun removeMod(filePath: String) {
        modsList.removeAll { it.filePath == filePath }
        val mod = modsMap.remove(filePath)

        (mod as? PackwizMetaFile)?.toml?.update?.modrinth?.modId?.let { modrinthProjects -= it }
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
                    ModFileInfo.getFileInfo(newModFile)
                        ?.toPackwizMod(modpackLocation, modsPathStr, metafileExtension, null, false)
                        ?.let(::addOrReplaceModInList)
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
                    ),
                    options = project.pack.options?.copy(
                        acceptableGameVersions = extraAcceptableVersions.minecraft.toList(),
                        acceptableLoaders = extraAcceptableVersions.loaders.toList()
                    ) ?: OptionsToml(
                        acceptableGameVersions = extraAcceptableVersions.minecraft.toList(),
                        acceptableLoaders = extraAcceptableVersions.loaders.toList()
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

    fun addAdditionalMinecraft(version: String) {
        additionalMinecraftText = ""
        extraAcceptableVersions = extraAcceptableVersions.copy(minecraft = extraAcceptableVersions.minecraft + version)
    }

    fun removeAdditionalMinecraft(version: String) {
        extraAcceptableVersions = extraAcceptableVersions.copy(minecraft = extraAcceptableVersions.minecraft - version)
    }

    fun toggleAdditionalLoader(type: LoaderVersion.Type) {
        val newLoaders = if (extraAcceptableVersions.loaders.contains(type.packwizName)) {
            extraAcceptableVersions.loaders - type.packwizName
        } else {
            extraAcceptableVersions.loaders + type.packwizName
        }

        extraAcceptableVersions = extraAcceptableVersions.copy(loaders = newLoaders)
    }

    fun install(install: InstallOperation) {
        log.info("Installing: ${install.toInstall.map { it.name }}")

        for (mod in install.toInstall) {
            addOrReplaceModInList(mod.toPackwizMod(modpackLocation, modsPathStr, metafileExtension, null, false))
        }
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

    fun openModProject(mod: PackwizMod) {
        val key = getModProjectKey(mod)
        if (!openProjectWindows.containsKey(key)) {
            openProjectWindows += key to PackwizFileSel(mod, ViewType.BODY)
        }
    }

    fun openModrinthProject(projectId: String) {
        if (!openProjectWindows.containsKey(projectId)) {
            openProjectWindows += projectId to ModrinthProjectSel(projectId)
        }
    }

    private fun getModProjectKey(mod: PackwizMod): Any {
        return when {
            mod is PackwizMetaFile && mod.toml.update?.modrinth?.modId != null -> mod.toml.update.modrinth.modId
            mod is PackwizMetaFile && mod.toml.update?.curseforge?.projectId != null -> mod.toml.update.curseforge.projectId
            mod is PackwizMetaFile && mod.toml.download.url != null -> mod.toml.download.url
            else -> mod.filePath
        }
    }

    private data class LoaderVersionInput(val loaderVersion: String, val minecraftVersion: String)
}

sealed class ModpackComponentArgs {
    data class CreateNew(val newModpack: NewModpack) : ModpackComponentArgs()
    data class OpenExisting(val packFile: Path) : ModpackComponentArgs()
}
