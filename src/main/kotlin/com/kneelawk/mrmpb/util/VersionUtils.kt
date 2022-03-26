package com.kneelawk.mrmpb.util

import com.github.zafarkhaja.semver.ParseException
import com.github.zafarkhaja.semver.Version

object VersionUtils {
    fun isSemVer(version: String): Boolean {
        return try {
            Version.valueOf(version)
            true
        } catch (e: ParseException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}