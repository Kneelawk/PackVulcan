package com.kneelawk.packvulcan.model.packwiz

import com.moandjiezana.toml.Toml

fun <K, V> maybeMapOf(vararg pairs: Pair<K, V>?): Map<K, V> {
    val map = LinkedHashMap<K, V>()
    for (pair in pairs) {
        pair?.let { map.put(pair.first, pair.second) }
    }
    return map
}

fun <K, V> V.from(key: K): Pair<K, V> = Pair(key, this)

@Throws(LoadError::class)
fun Toml.mustGetString(key: String): String {
    return getString(key) ?: throw missing(key, this)
}

@Throws(LoadError::class)
fun Toml.mustGetTable(key: String): Toml {
    return getTable(key) ?: throw missing(key, this)
}

@Throws(LoadError::class)
fun Toml.mustGetTables(key: String): List<Toml> {
    return getTables(key) ?: throw missing(key, this)
}

@Throws(LoadError::class)
fun Toml.mustGetBoolean(key: String): Boolean {
    return getBoolean(key) ?: throw missing(key, this)
}

@Throws(LoadError::class)
fun Toml.mustGetLong(key: String): Long {
    return getLong(key) ?: throw missing(key, this)
}
