package com.kneelawk.packvulcan.ui.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

typealias ShortcutListener = () -> Unit

@Composable
fun rememberKeyboardTracker(): KeyboardTracker {
    return remember { KeyboardTracker() }
}

fun shortcut() = ShortcutBuilder()

@Composable
fun shortcuts(tracker: KeyboardTracker, pair: Pair<Shortcut, ShortcutListener>) {
    DisposableEffect(Unit) {
        tracker.addListener(pair.first, pair.second)

        onDispose {
            tracker.removeListener(pair.first, pair.second)
        }
    }
}

@Composable
fun shortcuts(tracker: KeyboardTracker, vararg pairs: Pair<Shortcut, ShortcutListener>) {
    DisposableEffect(Unit) {
        for (pair in pairs) {
            tracker.addListener(pair.first, pair.second)
        }

        onDispose {
            for (pair in pairs) {
                tracker.removeListener(pair.first, pair.second)
            }
        }
    }
}
