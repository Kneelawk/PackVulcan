package com.kneelawk.packvulcan.net.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.engine.image.ImageUtils
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.time.Duration
import javax.imageio.ImageIO

object ImageResource {
    private val modIconCache: AsyncCache<String, ImageBitmap?> =
        Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(500)
            .buildAsync()

    private suspend fun retrieveBufferedImage(url: String): BufferedImage? = withContext(Dispatchers.IO) {
        // TODO: filesystem image caching
        val response: HttpResponse = HTTP_CLIENT.get(url)
        // looks like an input stream is the only way to do this atm
        val inputStream = response.content.toInputStream()
        ImageIO.read(inputStream)
    }

    private suspend fun retrieveIcon(url: String, size: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        retrieveBufferedImage(url)?.let { ImageUtils.scaleImage(it, size).toComposeImageBitmap() }
    }

    suspend fun getModIcon(url: String): ImageBitmap? = modIconCache.suspendGet(url) {
        retrieveIcon(url, ImageUtils.MOD_ICON_SIZE)
    }
}