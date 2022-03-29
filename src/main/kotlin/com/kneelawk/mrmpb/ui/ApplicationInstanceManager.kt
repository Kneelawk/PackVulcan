package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf

object ApplicationInstanceManager {
    private val instances = mutableStateListOf<ApplicationInstance>()

    fun openInstance() {
        instances.add(ApplicationInstance())
    }

    @Composable
    fun compose(exitApplication: () -> Unit) {
        for (instance in instances) {
            key(instance) {
                instance.compose {
                    instances.remove(instance)
                    if (instances.isEmpty()) {
                        exitApplication()
                    }
                }
            }
        }
    }
}