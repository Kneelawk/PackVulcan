package com.kneelawk.packvulcan.model.modrinth.search.query

enum class SearchIndex(val apiName: String) {
    RELEVANCE("relevance"),
    DOWNLOADS("downloads"),
    FOLLOWS("follows"),
    NEWEST("newest"),
    UPDATED("updated")
}
