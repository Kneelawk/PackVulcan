package com.kneelawk.mrmpb.model.packwiz

import com.moandjiezana.toml.Toml
import java.io.IOException

sealed class LoadError(msg: String) : IOException(msg) {
    data class MissingElement(val elementName: String, val toml: Toml) :
        LoadError("Missing element '$elementName' toml: $toml")

    data class BadHashFormat(val format: String) :
        LoadError("Bad hash format '$format'. Supported formats are 'md5', 'murmur2', 'sha256', and 'sha512'.")

    data class BadSide(val side: String) :
        LoadError("Bad side type '$side'. Supported side types are 'both', 'client', and 'server'.")
}

fun missing(elementName: String, toml: Toml) = LoadError.MissingElement(elementName, toml)
