package com.kneelawk.mrmpb.util

import java.nio.file.InvalidPathException
import java.nio.file.Paths

object PathUtils {
    fun isPathValid(string: String): Boolean {
        return try {
            Paths.get(string)
            true
        } catch (e: InvalidPathException) {
            false
        }
    }
}