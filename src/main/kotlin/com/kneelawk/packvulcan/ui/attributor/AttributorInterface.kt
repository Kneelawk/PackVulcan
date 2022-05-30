package com.kneelawk.packvulcan.ui.attributor

interface AttributorInterface {
    val attributionText: String
    val loading: Boolean

    fun generateAttributions()
}
