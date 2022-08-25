package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.model.modrinth.search.query.SearchIndex
import com.kneelawk.packvulcan.model.modrinth.search.query.SearchQuery
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.util.Conflator
import com.kneelawk.packvulcan.util.add
import mu.KotlinLogging

@Composable
fun rememberModrinthSearchController(
    selectedMinecraftVersions: MutableMap<String, Unit>, selectedKnownLoaders: MutableMap<LoaderVersion.Type, Unit>
): ModrinthSearchInterface {
    val log = remember { KotlinLogging.logger { } }
    val scope = rememberCoroutineScope()

    val searchResults = remember { mutableStateListOf<SearchHitDisplay>() }

    val loadingState = remember { mutableStateOf(true) }
    var loading by loadingState
    val minecraftLoadingState = remember { mutableStateOf(true) }
    var minecraftLoading by minecraftLoadingState
    val loadersLoadingState = remember { mutableStateOf(true) }
    var loadersLoading by loadersLoadingState
    val categoriesLoadingState = remember { mutableStateOf(true) }
    var categoriesLoading by categoriesLoadingState

    val searchStringState = remember { mutableStateOf("") }
    var searchStringC by searchStringState

    val minecraftVersions = remember { mutableStateListOf<MinecraftVersion>() }

    val loaderList = remember { mutableStateListOf<LoaderDisplay>() }
    val selectedLoaders = remember { mutableStateMapOf<LoaderDisplay, Unit>() }
    var shouldUpdateKnownLoaders by remember { mutableStateOf(false) }

    val filterClientState = remember { mutableStateOf(false) }
    var filterClientC by filterClientState
    val filterServerState = remember { mutableStateOf(false) }
    var filterServerC by filterServerState

    val categoryList = remember { mutableStateListOf<CategoryDisplay>() }
    val selectedCategories = remember { mutableStateMapOf<CategoryDisplay, Unit>() }

    val minecraftSelectorEnabledState = remember { derivedStateOf { !minecraftLoading } }
    val loaderSelectorEnabledState = remember { derivedStateOf { !loadersLoading } }
    val categorySelectorEnabledState = remember { derivedStateOf { !categoriesLoading } }

    val showMinecraftExtraTypesState = remember { mutableStateOf(false) }
    val showMinecraftReleasesState = remember { mutableStateOf(true) }
    val showMinecraftReleasesC by showMinecraftReleasesState
    val showMinecraftSnapshotsState = remember { mutableStateOf(false) }
    val showMinecraftSnapshotsC by showMinecraftSnapshotsState
    val showMinecraftBetasState = remember { mutableStateOf(false) }
    val showMinecraftBetasC by showMinecraftBetasState
    val showMinecraftAlphasState = remember { mutableStateOf(false) }
    val showMinecraftAlphasC by showMinecraftAlphasState

    val currentPageState = remember { mutableStateOf(1) }
    var currentPageC by currentPageState
    val finalPageState = remember { mutableStateOf(1) }
    var finalPageC by finalPageState

    val searchScrollState = rememberLazyListState()

    LaunchedEffect(showMinecraftReleasesC, showMinecraftSnapshotsC, showMinecraftBetasC, showMinecraftAlphasC) {
        minecraftLoading = true
        minecraftVersions.clear()
        minecraftVersions.addAll(MinecraftVersion.minecraftVersionList().filter {
            when (it.type) {
                MinecraftVersion.Type.OLD_ALPHA -> showMinecraftAlphasC
                MinecraftVersion.Type.OLD_BETA -> showMinecraftBetasC
                MinecraftVersion.Type.RELEASE -> showMinecraftReleasesC
                MinecraftVersion.Type.SNAPSHOT -> showMinecraftSnapshotsC
            }
        })
        minecraftLoading = false
    }

    LaunchedEffect(Unit) {
        loadersLoading = true
        loaderList.addAll(LoaderDisplay.loaderList().filter { MOD_LOADERS.contains(it.apiName) })

        val map = mutableMapOf<LoaderDisplay, Unit>()
        for (type in selectedKnownLoaders.keys) {
            LoaderDisplay.forType(type)?.let { map.add(it) }
        }
        selectedLoaders.putAll(map)
        loadersLoading = false
    }

    LaunchedEffect(Unit) {
        categoriesLoading = true
        categoryList.addAll(CategoryDisplay.categoryList().filter { it.projectType == "mod" })
        categoriesLoading = false
    }

    val searchConflator = remember {
        Conflator(scope) { data: SearchData ->
            loading = true
            val res = ModrinthApi.search(data.toQuery())
            finalPageC = (res.totalHits + SEARCH_RESULT_LIMIT - 1) / SEARCH_RESULT_LIMIT
            searchResults.clear()
            searchResults.addAll(res.hits.map { SearchHitDisplay.fromJson(it) })
            loading = false

            searchScrollState.scrollToItem(0)
        }
    }

    fun startSearch() {
        searchConflator.send(
            SearchData(
                searchStringC, selectedMinecraftVersions.keys.toSet(), selectedLoaders.keys.toSet(),
                selectedCategories.keys.toSet(), filterClientC, filterServerC, currentPageC
            )
        )
    }

    LaunchedEffect(Unit) {
        startSearch()
    }

    return remember {
        object : ModrinthSearchInterface {
            override val searchLoading by loadingState
            override val minecraftSelectorEnabled by minecraftSelectorEnabledState
            override val minecraftSelectorLoading by minecraftLoadingState
            override val loaderSelectorEnabled by loaderSelectorEnabledState
            override val loaderSelectorLoading by loadersLoadingState
            override val categorySelectorEnabled by categorySelectorEnabledState
            override val categorySelectorLoading by categoriesLoadingState
            override var showMinecraftExtraTypes by showMinecraftExtraTypesState
            override var showMinecraftReleases by showMinecraftReleasesState
            override var showMinecraftSnapshots by showMinecraftSnapshotsState
            override var showMinecraftBetas by showMinecraftBetasState
            override var showMinecraftAlphas by showMinecraftAlphasState
            override val minecraftVersionList = minecraftVersions
            override val selectedMinecraftVersions = selectedMinecraftVersions
            override val loaderList = loaderList
            override val selectedLoaders = selectedLoaders
            override val filterClient by filterClientState
            override val filterServer by filterServerState
            override val categoryList = categoryList
            override val selectedCategories = selectedCategories
            override val searchString by searchStringState
            override val searchResults = searchResults
            override val currentPage by currentPageState
            override val finalPage by finalPageState
            override val searchScrollState = searchScrollState

            override fun clearFilters() {
                selectedMinecraftVersions.clear()
                selectedLoaders.clear()
                selectedKnownLoaders.clear()
                filterClientC = false
                filterServerC = false
                selectedCategories.clear()

                currentPageC = 1
                startSearch()
            }

            override fun selectMinecraftVersion(version: String) {
                selectedMinecraftVersions.add(version)
                currentPageC = 1
                startSearch()
            }

            override fun unselectMinecraftVersion(version: String) {
                selectedMinecraftVersions.remove(version)
                currentPageC = 1
                startSearch()
            }

            override fun selectLoader(loader: LoaderDisplay) {
                selectedLoaders.add(loader)
                loader.loaderType?.let { selectedKnownLoaders.add(it) }
                currentPageC = 1
                startSearch()
            }

            override fun unselectLoader(loader: LoaderDisplay) {
                selectedLoaders.remove(loader)
                loader.loaderType?.let { selectedKnownLoaders.remove(it) }
                currentPageC = 1
                startSearch()
            }

            override fun selectCategory(category: CategoryDisplay) {
                selectedCategories.add(category)
                currentPageC = 1
                startSearch()
            }

            override fun unselectCategory(category: CategoryDisplay) {
                selectedCategories.remove(category)
                currentPageC = 1
                startSearch()
            }

            override fun setFilterClient(filter: Boolean) {
                filterClientC = filter
                currentPageC = 1
                startSearch()
            }

            override fun setFilterServer(filter: Boolean) {
                filterServerC = filter
                currentPageC = 1
                startSearch()
            }

            override fun setSearchString(string: String) {
                searchStringC = string
                currentPageC = 1
                startSearch()
            }

            override fun goToPage(page: Int) {
                currentPageC = page
                startSearch()
            }

            override fun pageForward() {
                if (currentPageC < finalPageC) currentPageC++
                startSearch()
            }

            override fun pageBackward() {
                if (currentPageC > 1) currentPageC--
                startSearch()
            }
        }
    }
}

val SEARCH_RESULT_LIMIT = 20

data class SearchData(
    val searchString: String, val minecraftVersions: Set<String>, val loaders: Set<LoaderDisplay>,
    val categories: Set<CategoryDisplay>, val filterClient: Boolean, val filterServer: Boolean, val currentPage: Int
) {
    fun toQuery(): SearchQuery {
        val query = searchString.ifBlank { null }

        val facets = mutableListOf(listOf("project_type:mod"))

        if (minecraftVersions.isNotEmpty()) {
            facets.add(minecraftVersions.map { "versions:$it" })
        }

        if (loaders.isNotEmpty()) {
            facets.add(loaders.map { "categories:${it.apiName}" })
        } else {
            facets.add(MOD_LOADERS.map { "categories:$it" })
        }

        if (categories.isNotEmpty()) {
            facets.add(categories.map { "categories:${it.apiName}" })
        }

        if (filterClient && filterServer) {
            facets.add(listOf("client_side:required"))
            facets.add(listOf("server_side:required"))
        } else if (filterClient) {
            facets.add(listOf("client_side:optional", "client_side:required"))
            facets.add(listOf("server_side:optional", "server_side:unsupported"))
        } else if (filterServer) {
            facets.add(listOf("client_side:optional", "client_side:unsupported"))
            facets.add(listOf("server_side:optional", "server_side:required"))
        }

        val offset = (currentPage - 1) * SEARCH_RESULT_LIMIT

        return SearchQuery(query, facets, SearchIndex.RELEVANCE, offset, SEARCH_RESULT_LIMIT)
    }
}
