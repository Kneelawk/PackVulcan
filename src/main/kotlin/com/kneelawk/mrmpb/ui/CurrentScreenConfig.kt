package com.kneelawk.mrmpb.ui

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

sealed class CurrentScreenConfig : Parcelable {
    @Parcelize
    object Start : CurrentScreenConfig()

    @Parcelize
    object Settings : CurrentScreenConfig()

    @Parcelize
    object CreateNew : CurrentScreenConfig()

    @Parcelize
    object Modpack : CurrentScreenConfig()
}
