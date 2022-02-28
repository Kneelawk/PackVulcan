package com.kneelawk.mrmpb.ui.util

import com.arkivanov.decompose.router.Router

fun <C : Any> Router<C, *>.popSafe() {
    navigate {
        if (it.size > 1) {
            it.dropLast(1)
        } else {
            it
        }
    }
}

fun <C : Any> Router<C, *>.replace(config: C) {
    navigate { listOf(config) }
}
