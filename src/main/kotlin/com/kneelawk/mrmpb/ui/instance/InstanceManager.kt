package com.kneelawk.mrmpb.ui.instance

import androidx.compose.runtime.*

object InstanceManager {
    private val instances = mutableStateListOf<Instance>()
    private var settings by mutableStateOf<SettingsInstance?>(null)

    fun newRoot() {
        instances.add(RootInstance())
    }

    fun openSettings() {
        settings = SettingsInstance
    }

    @Composable
    fun compose(exitApplication: () -> Unit) {
        for (instance in instances) {
            key(instance) {
                instance.compose {
                    instances.remove(instance)
                    // I was thinking to check if the settings window was open here, but I realized it actually doesn't
                    // make that much sense to keep the application running if the only open window is the settings
                    // window.
                    if (instances.isEmpty()) {
                        exitApplication()
                    }
                }
            }
        }

        settings?.let { instance ->
            instance.compose {
                settings = null
                if (instances.isEmpty()) {
                    exitApplication()
                }
            }
        }
    }
}