package com.kneelawk.mrmpb.ui

sealed class CurrentScreen {
    object Start : CurrentScreen()
    object CreateNew : CurrentScreen()
    class OpenExisting(existing: String) : CurrentScreen()
}
