package com.kneelawk.packvulcan.ui.util

import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.navigate

fun <C : Any> StackNavigator<C>.popSafe() {
    navigate {
        if (it.size > 1) {
            it.dropLast(1)
        } else {
            it
        }
    }
}

fun <C : Any> StackNavigator<C>.replace(config: C) {
    navigate { listOf(config) }
}
