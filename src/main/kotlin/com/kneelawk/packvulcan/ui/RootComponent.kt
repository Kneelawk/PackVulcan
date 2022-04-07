package com.kneelawk.packvulcan.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.ui.util.popSafe
import com.kneelawk.packvulcan.ui.util.replace
import java.nio.file.Path

class RootComponent(context: ComponentContext) : ComponentContext by context {
    private val router = router<CurrentScreenConfig, CurrentScreen>(
        initialConfiguration = CurrentScreenConfig.Start,
        handleBackButton = true,
        childFactory = ::componentFactory
    )

    val routerState = router.state

    private var modpackComponentArgs: ModpackComponentArgs? = null

    private fun componentFactory(config: CurrentScreenConfig, context: ComponentContext): CurrentScreen {
        return when (config) {
            CurrentScreenConfig.Start -> CurrentScreen.Start
            CurrentScreenConfig.CreateNew -> CurrentScreen.CreateNew(CreateNewComponent(context) { result ->
                when (result) {
                    CreateNewResult.Cancel -> goBack()
                    is CreateNewResult.Create -> createNewModpack(result.modpack)
                }
            })
            is CurrentScreenConfig.Modpack -> CurrentScreen.Modpack(
                ModpackComponent(
                    context, modpackComponentArgs
                        ?: throw IllegalStateException("Tried to open the modpack component without a modpack")
                )
            )
        }
    }

    fun openCreateNew() {
        router.push(CurrentScreenConfig.CreateNew)
    }

    fun goBack() {
        router.popSafe()
    }

    fun createNewModpack(newModpack: NewModpack) {
        modpackComponentArgs = ModpackComponentArgs.CreateNew(newModpack)
        router.replace(CurrentScreenConfig.Modpack)
    }

    fun openModpack(packFile: Path) {
        modpackComponentArgs = ModpackComponentArgs.OpenExisting(packFile)
        router.replace(CurrentScreenConfig.Modpack)
    }
}