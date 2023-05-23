package com.kneelawk.packvulcan.net.image

import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.ui.util.ImageWrapper
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CancellationException
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

    private suspend fun retrieveIcon(url: String): ImageWrapper? = withContext(Dispatchers.IO) {
        try {
            // TODO: filesystem image caching
            val response: HttpResponse = HTTP_CLIENT.get(url)

            // looks like an input stream is the only way to do this atm
            response.bodyAsChannel().toInputStream().use { inputStream ->
                if (response.contentType()?.match(ContentType.Image.SVG) == true) {
                    try {
                        ImageWrapper.Painter(loadSvgPainter(inputStream, Density(1f)))
                    } catch (e: Exception) {
                        log.warn("Error loading SVG {}", url, e)
                        null
                    }
                } else {
                    ImageIO.createImageInputStream(inputStream).use { imageStream ->
                        val readers = ImageIO.getImageReaders(imageStream)

                        if (!readers.hasNext()) {
                            log.warn("No image readers for {}", url)
                        }

                        while (readers.hasNext()) {
                            try {
                                val reader = readers.next()

                                reader.input = imageStream

                                reader.addIIOReadWarningListener { _, warning ->
                                    log.warn("Image reader warning {}: {}", url, warning)
                                }

                                return@withContext ImageWrapper.ImageBitmap(reader.read(0).toComposeImageBitmap())
                            } catch (e: Exception) {
                                log.warn("Error reading image {}", url, e)
                            }
                        }

                        null
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Error loading image {}", url, e)
            null
        }
    }

    suspend fun getModIcon(url: String): ImageWrapper? = modIconCache.suspendGet(url) {
        retrieveIcon(url)
    }
}
