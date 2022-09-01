package com.kneelawk.packvulcan.util

typealias MSet<T> = Map<T, Unit>
typealias MutableMSet<T> = MutableMap<T, Unit>

fun <T> MutableMap<T, Unit>.add(element: T): Boolean {
    return put(element, Unit) != null
}

fun <T> MutableMap<T, Unit>.addAll(elements: Iterable<T>): Boolean {
    var modified = false
    for (element in elements) {
        modified = put(element, Unit) != null || modified
    }
    return modified
}
