package com.kneelawk.packvulcan.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.ui.util.popSafe
import com.kneelawk.packvulcan.ui.util.replace
import java.nio.file.Path

class RootComponent(context: ComponentContext, initialState: RootInitialState) :
    ComponentContext by context {
    private val router: Router<CurrentScreenConfig, CurrentScreen>

    private var modpackComponentArgs: ModpackComponentArgs? = null

    init {
        // this is so cursed
        when (initialState) {
            RootInitialState.None -> {
                router = router(
                    initialConfiguration = CurrentScreenConfig.Start,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
            }

            RootInitialState.CreateNew -> {
                router = router(
                    initialConfiguration = CurrentScreenConfig.Start,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
                router.push(CurrentScreenConfig.CreateNew)
            }

            is RootInitialState.Open -> {
                modpackComponentArgs = ModpackComponentArgs.OpenExisting(initialState.path)
                router = router(
                    initialConfiguration = CurrentScreenConfig.Modpack,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
            }
        }
    }

    val routerState = router.state

    val windowControls = WindowControls(DEFAULT_WINDOW_TITLE)

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
