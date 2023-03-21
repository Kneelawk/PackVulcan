package com.kneelawk.packvulcan.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.ui.util.popSafe
import com.kneelawk.packvulcan.ui.util.replace
import java.nio.file.Path

class RootComponent(context: ComponentContext, initialState: RootInitialState) :
    ComponentContext by context {
    private val childNavigation = StackNavigation<CurrentScreenConfig>()
    val childStack: Value<ChildStack<CurrentScreenConfig, CurrentScreen>>

    private var modpackComponentArgs: ModpackComponentArgs? = null

    init {
        // this is so cursed
        when (initialState) {
            RootInitialState.None -> {
                childStack = childStack(
                    source = childNavigation,
                    initialConfiguration = CurrentScreenConfig.Start,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
            }

            RootInitialState.CreateNew -> {
                childStack = childStack(
                    source = childNavigation,
                    initialConfiguration = CurrentScreenConfig.Start,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
                childNavigation.push(CurrentScreenConfig.CreateNew)
            }

            is RootInitialState.Open -> {
                modpackComponentArgs = ModpackComponentArgs.OpenExisting(initialState.path)
                childStack = childStack(
                    source = childNavigation,
                    initialConfiguration = CurrentScreenConfig.Modpack,
                    handleBackButton = true,
                    childFactory = ::componentFactory
                )
            }
        }
    }

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
        childNavigation.push(CurrentScreenConfig.CreateNew)
    }

    fun goBack() {
        childNavigation.popSafe()
    }

    fun createNewModpack(newModpack: NewModpack) {
        modpackComponentArgs = ModpackComponentArgs.CreateNew(newModpack)
        childNavigation.replace(CurrentScreenConfig.Modpack)
    }

    fun openModpack(packFile: Path) {
        modpackComponentArgs = ModpackComponentArgs.OpenExisting(packFile)
        childNavigation.replace(CurrentScreenConfig.Modpack)
    }
}
