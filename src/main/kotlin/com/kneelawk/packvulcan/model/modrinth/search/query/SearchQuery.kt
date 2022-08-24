package com.kneelawk.packvulcan.model.modrinth.search.query

data class SearchQuery(
    val query: String?,
    val facets: List<List<String>>?,
    val index: SearchIndex?,
    val offset: Int?,
    val limit: Int?
)
