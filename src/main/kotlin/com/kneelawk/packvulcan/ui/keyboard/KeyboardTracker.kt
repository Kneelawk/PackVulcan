package com.kneelawk.packvulcan.ui.keyboard

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type

class KeyboardTracker {
    private val shortcuts = mutableMapOf<Shortcut, MutableList<ShortcutListener>>()

    fun addListener(shortcut: Shortcut, listener: ShortcutListener) {
        val listeners = shortcuts.computeIfAbsent(shortcut) { mutableListOf() }
        listeners.add(listener)
    }

    fun removeListener(shortcut: Shortcut, listener: ShortcutListener) {
        shortcuts[shortcut]?.let { listeners ->
            listeners.remove(listener)
            if (listeners.isEmpty()) {
                shortcuts.remove(shortcut)
            }
        }
    }

    fun keyPressed(event: KeyEvent): Boolean {
        return if (event.type == KeyEventType.KeyDown) {
            val shortcut = Shortcut.fromKeyEvent(event)
            shortcuts[shortcut]?.let { listeners ->
                for (listener in listeners) {
                    listener()
                }
                true
            } ?: false
        } else {
            false
        }
    }
}
