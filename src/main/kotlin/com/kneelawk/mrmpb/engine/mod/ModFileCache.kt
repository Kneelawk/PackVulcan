package com.kneelawk.mrmpb.engine.mod

import com.kneelawk.mrmpb.GlobalConstants
import com.kneelawk.mrmpb.engine.hash.HashHelper
import com.kneelawk.mrmpb.model.packwiz.HashFormat
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.buffer
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name

object ModFileCache {
    private val CACHE_DIR = GlobalConstants.MOD_CACHE_DIR_PATH

    init {
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.createDirectories()
        }
    }

    private fun getPathForUrl(url: String): Path {
        val url1 = Url(url)
        return CACHE_DIR.resolve(url1.host).resolve(url1.encodedPath)
    }

    suspend fun getModFile(downloadUrl: String, hash: String, hashFormat: HashFormat): Path =
        withContext(Dispatchers.IO) {
            val output = getPathForUrl(downloadUrl)
            val hashFile = output.parent.resolve(output.name + "." + hashFormat.toString())

            val curHash = if (hashFile.exists()) {
                FileSystem.SYSTEM.read(hashFile.toOkioPath()) {
                    readString(Charsets.UTF_8)
                }
            } else {
                val calculated = HashHelper.hash(output, hashFormat)
                FileSystem.SYSTEM.write(hashFile.toOkioPath()) {
                    writeUtf8(calculated)
                }
                calculated
            }

            if (output.exists() && curHash == hash) {
                output
            } else {
                if (!output.parent.exists()) {
                    output.parent.createDirectories()
                }

                val response: HttpResponse = HTTP_CLIENT.get(downloadUrl)

                val fileSink = FileSystem.SYSTEM.sink(output.toOkioPath())
                val hashSink = hashFormat.makeSink(fileSink)
                hashSink.buffer().use { response.content.copyTo(it.outputStream()) }

                FileSystem.SYSTEM.write(hashFile.toOkioPath()) {
                    writeUtf8(hashSink.hashString())
                }

                output
            }
        }
}