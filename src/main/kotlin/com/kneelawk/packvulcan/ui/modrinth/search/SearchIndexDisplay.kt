package com.kneelawk.packvulcan.ui.modrinth.search

import com.kneelawk.packvulcan.model.modrinth.search.query.SearchIndex

enum class SearchIndexDisplay(val value: SearchIndex, val prettyName: String) {
    RELEVANCE(SearchIndex.RELEVANCE, "Relevance"),
    DOWNLOADS(SearchIndex.DOWNLOADS, "Download Count"),
    FOLLOWS(SearchIndex.FOLLOWS, "Follow Count"),
    NEWEST(SearchIndex.NEWEST, "Recently Created"),
    UPDATED(SearchIndex.UPDATED, "Recently Updated")
}
