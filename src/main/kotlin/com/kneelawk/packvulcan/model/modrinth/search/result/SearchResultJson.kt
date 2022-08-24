package com.kneelawk.packvulcan.model.modrinth.search.result

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultJson(
    val hits: List<SearchHitJson>,
    val offset: Int,
    val limit: Int,
    @SerialName("total_hits")
    val totalHits: Int
)
