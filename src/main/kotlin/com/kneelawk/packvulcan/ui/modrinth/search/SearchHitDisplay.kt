package com.kneelawk.packvulcan.ui.modrinth.search

import com.kneelawk.packvulcan.model.modrinth.search.result.SearchHitJson
import com.kneelawk.packvulcan.ui.modrinth.CategoryDisplay
import com.kneelawk.packvulcan.ui.modrinth.LoaderDisplay
import com.kneelawk.packvulcan.util.ifNullOrEmpty
import mu.KotlinLogging
import java.time.ZonedDateTime

data class SearchHitDisplay(
    val slug: String,
    val title: String,
    val author: String,
    val description: String,
    val iconUrl: String?,
    val categories: List<CategoryDisplay>,
    val loaders: List<LoaderDisplay>,
    val downloads: Int,
    val follows: Int,
    val dateCreated: ZonedDateTime,
    val dateModified: ZonedDateTime
) {
    companion object {
        private val log = KotlinLogging.logger { }

        suspend fun fromJson(json: SearchHitJson): SearchHitDisplay {
            val categoryMap = CategoryDisplay.categoryNameMap()
            val loaderMap = LoaderDisplay.loaderNameMap()

            val categories = mutableListOf<CategoryDisplay>()
            val loaders = mutableListOf<LoaderDisplay>()

            val displayCategories = json.displayCategories.ifNullOrEmpty { json.categories }
            if (displayCategories != null) {
                for (category in displayCategories) {
                    val categoryDisplay = categoryMap[category]
                    val loaderDisplay = loaderMap[category]

                    if (categoryDisplay != null) {
                        categories.add(categoryDisplay)
                    } else if (loaderDisplay != null) {
                        loaders.add(loaderDisplay)
                    } else {
                        log.warn("Encountered unknown category: {}", category)
                    }
                }
            }

            return SearchHitDisplay(
                json.slug, json.title, json.author, json.description, json.iconUrl, categories, loaders, json.downloads,
                json.follows, json.dateCreated, json.dateModified
            )
        }
    }
}
