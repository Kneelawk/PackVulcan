package com.kneelawk.packvulcan.ui.modrinth

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.LoaderVersion
import com.kneelawk.packvulcan.model.modrinth.tag.LoaderJson
import com.kneelawk.packvulcan.net.modrinth.ModrinthApi
import com.kneelawk.packvulcan.ui.theme.PackVulcanIcons
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.ui.util.loadSvgPainter
import com.kneelawk.packvulcan.util.suspendGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.*

// FIXME: Hardcoded list of 'mod' loaders
val MOD_LOADERS = setOf("fabric", "forge", "quilt", "liteloader", "modloader", "rift")
val PRIMARY_MOD_LOADERS = setOf("fabric", "forge", "quilt")

class LoaderDisplay(
    override val prettyName: String, val apiName: String, val loaderType: LoaderVersion.Type?,
    override val icon: ImageWrapper?
) : DisplayElement {
    companion object {
        private val log = KotlinLogging.logger { }

        private fun getPainterFor(json: LoaderJson): Painter? {
            return when (json.name) {
                "fabric" -> PackVulcanIcons.fabric
                "forge" -> PackVulcanIcons.forge
                "quilt" -> PackVulcanIcons.quilt
                else -> try {
                    loadSvgPainter(json.icon, Density(1f))
                } catch (e: Exception) {
                    log.warn("Error loading loader category SVG: {}", json.name)
                    null
                }
            }
        }

        private fun getLoaderTypeFor(json: LoaderJson): LoaderVersion.Type? {
            return when (json.name) {
                "fabric" -> LoaderVersion.Type.FABRIC
                "liteloader" -> LoaderVersion.Type.LITELOADER
                "modloader" -> LoaderVersion.Type.MODLOADER
                "forge" -> LoaderVersion.Type.FORGE
                "quilt" -> LoaderVersion.Type.QUILT
                "rift" -> LoaderVersion.Type.RIFT
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
            val icon = getPainterFor(json)?.let { ImageWrapper.Painter(it) }

            LoaderDisplay(prettyName, json.name, type, icon)
        }

        private val loaderCache: AsyncCache<Unit, List<LoaderDisplay>> =
            Caffeine.newBuilder().buildAsync()
        private val loaderTypeCache: AsyncCache<Unit, Map<LoaderVersion.Type, LoaderDisplay>> =
            Caffeine.newBuilder().buildAsync()
        private val loaderNameCache: AsyncCache<Unit, Map<String, LoaderDisplay>> =
            Caffeine.newBuilder().buildAsync()

        suspend fun loaderList(): List<LoaderDisplay> = loaderCache.suspendGet(Unit) {
            ModrinthApi.loaders().map { fromApi(it) }
        }

        private suspend fun loaderTypeMap(): Map<LoaderVersion.Type, LoaderDisplay> = loaderTypeCache.suspendGet(Unit) {
            loaderList().asSequence().filter { it.loaderType != null }.associateBy { it.loaderType!! }
        }

        suspend fun forType(type: LoaderVersion.Type): LoaderDisplay? {
            return loaderTypeMap()[type]
        }

        suspend fun loaderNameMap(): Map<String, LoaderDisplay> = loaderNameCache.suspendGet(Unit) {
            loaderList().associateBy { it.apiName }
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
