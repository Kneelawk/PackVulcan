package com.kneelawk.packvulcan.util

inline fun <R : Collection<*>?, T : R> T?.ifNullOrEmpty(default: () -> R): R {
    return if (this.isNullOrEmpty()) {
        default()
    } else {
        this
    }
}
