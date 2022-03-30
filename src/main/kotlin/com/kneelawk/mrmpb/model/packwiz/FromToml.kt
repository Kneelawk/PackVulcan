package com.kneelawk.mrmpb.model.packwiz

import com.moandjiezana.toml.Toml

interface FromToml<T> {
    @Throws(LoadError::class)
    fun fromToml(toml: Toml): T
}