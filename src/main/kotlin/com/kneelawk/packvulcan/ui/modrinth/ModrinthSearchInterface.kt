package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.foundation.lazy.LazyListState
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.model.modrinth.search.result.SearchHitJson

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
    val selectedMinecraftVersions: Map<String, Unit>
    val loaderList: List<LoaderDisplay>
    val selectedLoaders: Map<LoaderDisplay, Unit>
    val filterClient: Boolean
    val filterServer: Boolean
    val categoryList: List<CategoryDisplay>
    val selectedCategories: Map<CategoryDisplay, Unit>
    val searchString: String
    val searchResults: List<SearchHitJson>
    val currentPage: Int
    val finalPage: Int
    val searchScrollState: LazyListState

    fun clearFilters()

    fun selectMinecraftVersion(version: String)

    fun unselectMinecraftVersion(version: String)

    fun selectLoader(loader: LoaderDisplay)

    fun unselectLoader(loader: LoaderDisplay)

    fun selectCategory(category: CategoryDisplay)

    fun unselectCategory(category: CategoryDisplay)

    fun setFilterClient(filter: Boolean)

    fun setFilterServer(filter: Boolean)

    fun setSearchString(string: String)

    fun goToPage(page: Int)

    fun pageForward()

    fun pageBackward()
}
