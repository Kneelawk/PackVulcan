package com.kneelawk.packvulcan.ui.modrinth

import com.kneelawk.packvulcan.model.MinecraftVersion

interface ModrinthSearchInterface {
    val searchLoading: Boolean
    val minecraftSelectorEnabled: Boolean
    val minecraftSelectorLoading: Boolean
    val loaderSelectorEnabled: Boolean
    val loaderSelectorLoading: Boolean
    val categorySelectorEnabled: Boolean
    val categorySelectorLoading: Boolean
    var showMinecraftExtraTypes: Boolean
    var showMinecraftReleases: Boolean
    var showMinecraftSnapshots: Boolean
    var showMinecraftBetas: Boolean
    var showMinecraftAlphas: Boolean
    val minecraftVersionList: List<MinecraftVersion>
    val selectedMinecraftVersions: MutableMap<String, Unit>
    val loaderList: List<LoaderDisplay>
    val selectedLoaders: Map<LoaderDisplay, Unit>
    var filterClient: Boolean
    var filterServer: Boolean
    val categoryList: List<CategoryDisplay>
    val selectedCategories: MutableMap<CategoryDisplay, Unit>

    fun clearFilters()

    fun selectLoader(loader: LoaderDisplay)

    fun unselectLoader(loader: LoaderDisplay)
}