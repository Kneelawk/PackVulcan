package com.kneelawk.packvulcan.util

object OsUtils {
    private val OS_NAME = System.getProperty("os.name")
    val IS_WINDOWS = OS_NAME.startsWith("Windows")
    val IS_MAC = OS_NAME.startsWith("Mac")
}