package com.kneelawk.mrmpb.ui.util.dialog

interface CreateFolderInterface {
    val folderName: String
    val folderNameValid: Boolean

    fun folderNameUpdate(newName: String)

    fun createFolder()

    fun cancel()
}