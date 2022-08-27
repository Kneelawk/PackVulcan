package com.kneelawk.packvulcan.model.packwiz

import com.moandjiezana.toml.Toml

interface FromTomlVersioned<T> {
    @Throws(LoadError::class)
    fun fromToml(toml: Toml, packFormat: FormatVersion): T
}
