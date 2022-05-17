package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.modrinth.tag.CategoryJson
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.loadSvgPainter
import com.kneelawk.packvulcan.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CategoryDisplay(override val prettyName: String, val apiName: String, val projectType: String, override val icon: @Composable () -> Unit) :
    DisplayElement {
    companion object {
        private fun getPainterFor(json: CategoryJson): Painter {
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
                else -> loadSvgPainter(json.icon, Density(1f))
            }
        }

        private suspend fun fromApi(json: CategoryJson): CategoryDisplay = withContext(Dispatchers.IO) {
            val prettyName = json.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
            val icon = getPainterFor(json)
            CategoryDisplay(prettyName, json.name, json.projectType) { Icon(icon, json.name, modifier = Modifier.size(24.dp)) }
        }

        private val categoryCache: AsyncCache<Unit, List<CategoryDisplay>> =
            Caffeine.newBuilder().buildAsync()

        suspend fun categoryList(): List<CategoryDisplay> = categoryCache.suspendGet(Unit) {
            ModrinthApi.categories().map { fromApi(it) }
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
