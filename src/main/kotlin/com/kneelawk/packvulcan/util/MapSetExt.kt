package com.kneelawk.packvulcan.util

fun <T> MutableMap<T, Unit>.add(element: T): Boolean {
    return put(element, Unit) != null
}
