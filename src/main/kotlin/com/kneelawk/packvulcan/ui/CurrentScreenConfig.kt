package com.kneelawk.packvulcan.ui

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

sealed class CurrentScreenConfig : Parcelable {
    @Parcelize
    object Start : CurrentScreenConfig()

    @Parcelize
    object CreateNew : CurrentScreenConfig()

    @Parcelize
    object Modpack : CurrentScreenConfig()
}
