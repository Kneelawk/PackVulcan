package com.kneelawk.packvulcan.ui.modrinth.detail

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kneelawk.packvulcan.model.ModrinthModInfo
import com.kneelawk.packvulcan.ui.detail.DetailSubView
import com.kneelawk.packvulcan.ui.util.IconWrapper
import com.kneelawk.packvulcan.ui.util.markdown.MarkdownView
import com.kneelawk.packvulcan.util.LoadingState

class ModrinthDetailSubView(val mod: ModrinthModInfo) : DetailSubView {
    override val title = mod.name
    override val description = mod.description
    override val supportsGallery = true
    override val supportsVersions = true
    override var modIcon by mutableStateOf<LoadingState<IconWrapper>>(LoadingState.Loading)

    override suspend fun loadModIcon() {
        modIcon = try {
            LoadingState.Loaded(IconWrapper.fromIconSource(mod.icon))
        } catch (e: Exception) {
            LoadingState.Error
        }
    }

    @Composable
    override fun doBody() {
        Row {
            val sidebarScrollState = rememberScrollState()

            Column(modifier = Modifier.fillMaxHeight().weight(1f).verticalScroll(sidebarScrollState)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    MarkdownView(mod.body)
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(sidebarScrollState),
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }

    @Composable
    override fun doGallery() {
    }

    @Composable
    override fun doVersions() {
    }
}
