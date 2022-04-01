package com.kneelawk.mrmpb.ui.keyboard

import androidx.compose.ui.input.key.*
import com.kneelawk.mrmpb.util.OsUtils
import java.awt.event.KeyEvent.getKeyText

data class Shortcut(
    val key: Key, val shift: Boolean = false, val ctrl: Boolean = false, val alt: Boolean = false,
    val logo: Boolean = false
) {
    companion object {
        fun fromKeyEvent(keyEvent: KeyEvent): Shortcut {
            return Shortcut(
                keyEvent.key, keyEvent.isShiftPressed, keyEvent.isCtrlPressed, keyEvent.isAltPressed,
                keyEvent.isMetaPressed
            )
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if (OsUtils.IS_MAC) {
            if (ctrl) {
                builder.append("^-")
            }
            if (logo) {
                builder.append("\u2318-")
            }
            if (shift) {
                builder.append("\u21E7-")
            }
            if (alt) {
                builder.append("\u2325-")
            }
        } else {
            if (ctrl) {
                builder.append("Ctrl+")
            }
            if (logo) {
                builder.append("Logo+")
            }
            if (shift) {
                builder.append("Shift+")
            }
            if (alt) {
                builder.append("Alt+")
            }
        }
        builder.append(getKeyText(key.nativeKeyCode))
        return builder.toString()
    }
}
