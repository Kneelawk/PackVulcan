package com.kneelawk.mrmpb.ui.util.dialog.file

interface CreateFolderInterface {
    val folderName: String
    val folderNameValid: Boolean
    val folderNameError: String?

    fun folderNameUpdate(newName: String)

    fun createFolder()

    fun cancel()
}