package com.kneelawk.mrmpb.ui

sealed class CurrentScreen {
    object Start : CurrentScreen()
    object Settings : CurrentScreen()
    class CreateNew(val component: CreateNewComponent) : CurrentScreen()
    class Modpack(val component: ModpackComponent) : CurrentScreen()
}
