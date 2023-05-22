package com.kneelawk.packvulcan.ui.detail

import com.kneelawk.packvulcan.util.LoadingState

interface DetailInterface {
    val subView: LoadingState<DetailSubView>

    val curTab: ViewType

    fun reloadSubViews()
}
