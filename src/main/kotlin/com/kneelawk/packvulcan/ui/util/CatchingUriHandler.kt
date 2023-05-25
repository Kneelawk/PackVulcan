package com.kneelawk.packvulcan.ui.util

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.UriHandler
import com.kneelawk.packvulcan.engine.modrinth.ModrinthUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CatchingUriHandler(
    private val delegate: UriHandler, private val scope: CoroutineScope,
    private val openModrinthProject: (String) -> Unit
) :
    UriHandler {
    companion object {
        private val MODRINTH_URI = Regex("""modrinth\.com/mod/(?<slug>[^/]+)""")
    }

    override fun openUri(uri: String) {
        println("$ Opening URI: $uri")
        val modrinthMatch = MODRINTH_URI.find(uri)
        if (modrinthMatch != null) {
            val slug = modrinthMatch.groups["slug"]!!.value
            println("$   Match! Slug: $slug")
            scope.launch {
                val id = ModrinthUtils.getProjectId(slug)
                id?.let(openModrinthProject)
            }
        } else {
            println("$   No match.")
            delegate.openUri(uri)
        }
    }
}

val LocalCatchingUriHandler = staticCompositionLocalOf<UriHandler> {
    error("CompositionLocal LocalCatchingUriHandler not present")
}
