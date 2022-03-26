package com.kneelawk.mrmpb.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.kneelawk.mrmpb.util.VersionUtils

class CreateNewComponent(context: ComponentContext, private val finish: (CreateNewResult) -> Unit) :
    ComponentContext by context {
    var location by mutableStateOf("")
    var name by mutableStateOf("")
    var author by mutableStateOf("")
    var version by mutableStateOf("0.1.0")
    val versionError by derivedStateOf { !VersionUtils.isSemVer(version) }
    var minecraftVersion by mutableStateOf("1.18.2")
    val minecraftVersionError = false
    var loaderVersion by mutableStateOf("Fabric 0.13.3")
    val loaderVersionError = false

    val createEnabled by derivedStateOf {
        location.isNotBlank() && name.isNotBlank() && author.isNotBlank() && VersionUtils.isSemVer(version)
    }

    fun cancel() {
        finish(CreateNewResult.Cancel)
    }

    fun create() {
    }
}

sealed class CreateNewResult {
    object Cancel : CreateNewResult()
}
