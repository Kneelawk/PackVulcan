package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.modrinth.tag.CategoryJson
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.ui.util.loadSvgPainter
import com.kneelawk.packvulcan.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.*

class CategoryDisplay(
    override val prettyName: String, val apiName: String, val projectType: String,
    override val icon: ImageWrapper?
) :
    DisplayElement {
    companion object {
        private val log = KotlinLogging.logger { }

        private fun getPainterFor(json: CategoryJson): Painter? {
            return when (json.name) {
                "adventure" -> PackVulcanIcons.compass
                "cursed" -> PackVulcanIcons.bug
                "decoration" -> PackVulcanIcons.house
                "equipment" -> PackVulcanIcons.shield
                "food" -> PackVulcanIcons.restaurant
                "library" -> PackVulcanIcons.codeBox
                "magic" -> PackVulcanIcons.flame
                "optimization" -> PackVulcanIcons.bolt
                "storage" -> PackVulcanIcons.inventory
                "technology" -> PackVulcanIcons.microscope
                "utility" -> PackVulcanIcons.fancySuitcase
                "worldgen" -> PackVulcanIcons.public
                else -> if (json.icon.isNotBlank()) {
                    try {
                        loadSvgPainter(json.icon, Density(1f))
                    } catch (e: RuntimeException) {
                        log.warn("Encountered invalid SVG for category: '{}'", json.name)
                        null
                    }
                } else null
            }
        }

        private suspend fun fromApi(json: CategoryJson): CategoryDisplay = withContext(Dispatchers.IO) {
            val prettyName = json.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
            val icon = getPainterFor(json)?.let { ImageWrapper.Painter(it) }
            CategoryDisplay(prettyName, json.name, json.projectType, icon)
        }

        private val categoryCache: AsyncCache<Unit, List<CategoryDisplay>> =
            Caffeine.newBuilder().buildAsync()
        private val categoryNameCache: AsyncCache<Unit, Map<String, CategoryDisplay>> =
            Caffeine.newBuilder().buildAsync()

        suspend fun categoryList(): List<CategoryDisplay> = categoryCache.suspendGet(Unit) {
            ModrinthApi.categories().map { fromApi(it) }
        }

        suspend fun categoryNameMap(): Map<String, CategoryDisplay> = categoryNameCache.suspendGet(Unit) {
            categoryList().associateBy { it.apiName }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CategoryDisplay

        if (apiName != other.apiName) return false

        return true
    }

    override fun hashCode(): Int {
        return apiName.hashCode()
    }
}
