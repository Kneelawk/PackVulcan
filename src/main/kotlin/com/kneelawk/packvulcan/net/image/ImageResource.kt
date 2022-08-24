package com.kneelawk.packvulcan.net.image

import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.time.Duration
import javax.imageio.ImageIO

object ImageResource {
    private val log = KotlinLogging.logger { }

    private val modIconCache: AsyncCache<String, ImageWrapper?> =
        Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(500)
            .buildAsync()

    private suspend fun retrieveIcon(url: String, size: Int): ImageWrapper? = withContext(Dispatchers.IO) {
        try {
            // TODO: filesystem image caching
            val response: HttpResponse = HTTP_CLIENT.get(url)

            // looks like an input stream is the only way to do this atm
            val inputStream = response.bodyAsChannel().toInputStream()

            if (response.contentType()?.match(ContentType.Image.SVG) == true) {
                try {
                    ImageWrapper.Painter(loadSvgPainter(inputStream, Density(1f)))
                } catch (e: Exception) {
                    log.warn("Error loading SVG", e)
                    null
                }
            } else {
                ImageIO.read(inputStream)
                    ?.let { ImageWrapper.ImageBitmap(ImageUtils.scaleImage(it, size).toComposeImageBitmap()) }
            }
        } catch (e: Exception) {
            log.warn("Error loading image", e)
            null
        }
    }

    suspend fun getModIcon(url: String): ImageWrapper? = modIconCache.suspendGet(url) {
        retrieveIcon(url, ImageUtils.MOD_ICON_SIZE)
    }
}
