package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.mrmpb.engine.packwiz.PackwizProject
import com.kneelawk.mrmpb.model.LoaderVersionType
import com.kneelawk.mrmpb.model.NewModpack
import com.kneelawk.mrmpb.util.ComponentScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.nio.file.Path

class ModpackComponent(context: ComponentContext, args: ModpackComponentArgs) : ComponentContext by context {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val scope = ComponentScope(lifecycle)

    var loading by mutableStateOf(true)

    var modpackLocation by mutableStateOf("")
    var modpackName by mutableStateOf("")
    var modpackAuthor by mutableStateOf("")
    var modpackVersion by mutableStateOf("")
    var minecraftVersion by mutableStateOf("")
    var loaderVersion by mutableStateOf("")

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

            modpackLocation = project.projectDir.toString()
            modpackName = project.pack.name
            modpackAuthor = project.pack.author ?: ""
            modpackVersion = project.pack.version ?: ""
            minecraftVersion = project.pack.versions.minecraft

            loaderVersion = project.pack.versions.loaderVersions.entries.firstNotNullOfOrNull {
                LoaderVersionType.fromPackwizName(
                    it.key
                )?.to(it.value)
            }?.let {
                "${it.first.prettyName} ${it.second}"
            } ?: ""

            log.info("Writing packwiz project to '${project.projectDir}'...")
            project.write()

            loading = false
        }
    }
}

sealed class ModpackComponentArgs {
    data class CreateNew(val newModpack: NewModpack) : ModpackComponentArgs()
    data class OpenExisting(val packFile: Path) : ModpackComponentArgs()
}
