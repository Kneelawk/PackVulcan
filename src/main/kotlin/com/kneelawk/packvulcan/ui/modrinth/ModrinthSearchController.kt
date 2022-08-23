package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.runtime.*
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.MinecraftVersion
import com.kneelawk.packvulcan.util.add
import kotlinx.coroutines.delay

@Composable
fun rememberModrinthSearchController(
    selectedMinecraftVersions: MutableMap<String, Unit>, selectedKnownLoaders: MutableMap<LoaderVersion.Type, Unit>
): ModrinthSearchInterface {
    val loadingState = remember { mutableStateOf(true) }
    var loading by loadingState
    val minecraftLoadingState = remember { mutableStateOf(true) }
    var minecraftLoading by minecraftLoadingState
    val loadersLoadingState = remember { mutableStateOf(true) }
    var loadersLoading by loadersLoadingState
    val categoriesLoadingState = remember { mutableStateOf(true) }
    var categoriesLoading by categoriesLoadingState

    val minecraftVersions = remember { mutableStateListOf<MinecraftVersion>() }

    val loaderList = remember { mutableStateListOf<LoaderDisplay>() }
    val selectedLoaders = remember { mutableStateMapOf<LoaderDisplay, Unit>() }
    var shouldUpdateKnownLoaders by remember { mutableStateOf(false) }

    val filterClientState = remember { mutableStateOf(false) }
    val filterServerState = remember { mutableStateOf(false) }

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

    LaunchedEffect(selectedMinecraftVersions, selectedKnownLoaders, filterClientState, filterServerState) {
        loading = true
        // TODO: actually do search
        delay(500)
        loading = false
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
            override var filterClient by filterClientState
            override var filterServer by filterServerState
            override val categoryList = categoryList
            override val selectedCategories = selectedCategories

            override fun clearFilters() {
                selectedMinecraftVersions.clear()
                selectedLoaders.clear()
                filterClient = false
                filterServer = false
                selectedCategories.clear()
            }

            override fun selectLoader(loader: LoaderDisplay) {
                selectedLoaders.add(loader)
                loader.loaderType?.let { selectedKnownLoaders.add(it) }
            }

            override fun unselectLoader(loader: LoaderDisplay) {
                selectedLoaders.remove(loader)
                loader.loaderType?.let { selectedKnownLoaders.remove(it) }
            }
        }
    }
}
