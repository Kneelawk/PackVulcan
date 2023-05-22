package com.kneelawk.packvulcan.ui.detail

import androidx.compose.runtime.*
import com.kneelawk.packvulcan.util.LoadingState
import kotlinx.coroutines.launch

@Composable
fun rememberDetailController(selector: DetailSelector, updateTitle: (String) -> Unit): DetailInterface {
    val scope = rememberCoroutineScope()

    val subViewState = remember { mutableStateOf<LoadingState<DetailSubView>>(LoadingState.Loading) }

    val curTabState = remember(selector) { mutableStateOf(selector.viewType) }

    suspend fun loadSubView() {
        subViewState.value = selector.load()?.let { LoadingState.Loaded(it) } ?: LoadingState.Error
    }

    LaunchedEffect(Unit) {
        loadSubView()
    }

    LaunchedEffect(subViewState.value) {
        when (val subViewLoad = subViewState.value) {
            LoadingState.Error -> updateTitle("Error Loading Project")
            is LoadingState.Loaded -> updateTitle(subViewLoad.data.title)
            LoadingState.Loading -> updateTitle("Loading...")
        }
    }

    return remember {
        object : DetailInterface {
            override val subView by subViewState
            override val curTab by curTabState

            override fun reloadSubViews() {
                scope.launch {
                    loadSubView()
                }
            }
        }
    }
}
