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
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.modrinth.tag.LoaderJson
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.loadSvgPainter
import com.kneelawk.packvulcan.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.*

class LoaderDisplay(
    override val prettyName: String, val apiName: String, val loaderType: LoaderVersion.Type?,
    override val icon: @Composable () -> Unit
) : DisplayElement {
    companion object {
        private val log = KotlinLogging.logger { }

        private fun getPainterFor(json: LoaderJson): Painter {
            return when (json.name) {
                "fabric" -> PackVulcanIcons.fabric
                "forge" -> PackVulcanIcons.forge
                "quilt" -> PackVulcanIcons.quilt
                else -> loadSvgPainter(json.icon, Density(1f))
            }
        }

        private fun getLoaderTypeFor(json: LoaderJson): LoaderVersion.Type? {
            return when (json.name) {
                "fabric" -> LoaderVersion.Type.FABRIC
                "forge" -> LoaderVersion.Type.FORGE
                "quilt" -> LoaderVersion.Type.QUILT
                else -> {
                    log.warn("Received unknown loader from Modrinth: '${json.name}'")
                    null
                }
            }
        }

        private suspend fun fromApi(json: LoaderJson): LoaderDisplay = withContext(Dispatchers.IO) {
            val prettyName = json.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
            val type = getLoaderTypeFor(json)
            val icon = getPainterFor(json)

            LoaderDisplay(prettyName, json.name, type) { Icon(icon, json.name, modifier = Modifier.size(24.dp)) }
        }

        private val loaderCache: AsyncCache<Unit, List<LoaderDisplay>> =
            Caffeine.newBuilder().buildAsync()
        private val loaderMapCache: AsyncCache<Unit, Map<LoaderVersion.Type, LoaderDisplay>> =
            Caffeine.newBuilder().buildAsync()

        suspend fun loaderList(): List<LoaderDisplay> = loaderCache.suspendGet(Unit) {
            ModrinthApi.loaders().map { fromApi(it) }
        }

        private suspend fun loaderMap(): Map<LoaderVersion.Type, LoaderDisplay> = loaderMapCache.suspendGet(Unit) {
            loaderList().asSequence().filter { it.loaderType != null }.associateBy { it.loaderType!! }
        }

        suspend fun forType(type: LoaderVersion.Type): LoaderDisplay? {
            return loaderMap()[type]
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoaderDisplay

        if (apiName != other.apiName) return false

        return true
    }

    override fun hashCode(): Int {
        return apiName.hashCode()
    }
}
