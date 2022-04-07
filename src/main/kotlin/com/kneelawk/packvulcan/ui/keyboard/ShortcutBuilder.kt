package com.kneelawk.packvulcan.ui.keyboard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import com.kneelawk.packvulcan.util.OsUtils

@OptIn(ExperimentalComposeUiApi::class)
class ShortcutBuilder {
    private var shortcut = Shortcut(Key.Escape)

    fun key(key: Key): ShortcutBuilder {
        shortcut = shortcut.copy(key = key)
        return this
    }

    fun shift(): ShortcutBuilder {
        shortcut = shortcut.copy(shift = true)
        return this
    }

    fun ctrl(): ShortcutBuilder {
        shortcut = shortcut.copy(ctrl = true)
        return this
    }

    fun alt(): ShortcutBuilder {
        shortcut = shortcut.copy(alt = true)
        return this
    }

    fun logo(): ShortcutBuilder {
        shortcut = shortcut.copy(logo = true)
        return this
    }

    fun cmd(): ShortcutBuilder {
        return if (OsUtils.IS_MAC) {
            logo()
        } else {
            ctrl()
        }
    }

    fun finish() = shortcut
}
