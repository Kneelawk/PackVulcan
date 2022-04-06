package com.kneelawk.mrmpb.engine.mod

import com.kneelawk.mrmpb.GlobalConstants
import com.kneelawk.mrmpb.GlobalConstants.MAX_DOWNLOAD_ATTEMPTS
import com.kneelawk.mrmpb.engine.hash.HashHelper
import com.kneelawk.mrmpb.model.HashFormat
import com.kneelawk.mrmpb.model.cache.ModFileCacheJson
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import mu.KotlinLogging
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.buffer
import java.io.IOException
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name

object ModFileCache {
    private val log = KotlinLogging.logger { }

    private val ILLEGAL_CHARACTERS = Regex("""[^a-zA-Z0-9\-._ !@#$%^*(){}\[\]/]""")
    private val CACHE_DIR = GlobalConstants.MOD_CACHE_DIR_PATH

    init {
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.createDirectories()
        }
    }

    private fun getPathForUrl(url: String): Path {
        val url1 = Url(url)
        return CACHE_DIR.resolve(url1.host).resolve(sanitize(url1.encodedPath))
    }

    private fun sanitize(input: String): String {
        val decoded = input.decodeURLPart()
        val slashRemoved = if (decoded.startsWith("/")) {
            decoded.substring(1)
        } else {
            decoded
        }
        return slashRemoved.replace(ILLEGAL_CHARACTERS, "-")
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getModFile(downloadUrl: String, hash: String, hashFormat: HashFormat): Path =
        withContext(Dispatchers.IO) {
            val output = getPathForUrl(downloadUrl)
            val jsonFile = output.parent.resolve(output.name + ".cache-meta.json")

            // read metadata
            var json = if (jsonFile.exists()) {
                FileSystem.SYSTEM.read(jsonFile.toOkioPath()) {
                    Json.decodeFromStream(inputStream())
                }
            } else {
                ModFileCacheJson(ZonedDateTime.now(ZoneOffset.UTC), mapOf())
            }

            val curHash = if (json.hashes.containsKey(hashFormat)) {
                json.hashes[hashFormat]!!
            } else {
                if (output.exists()) {
                    val calculated = HashHelper.hash(output, hashFormat)

                    // write new metadata
                    json = json.copy(
                        lastAccess = ZonedDateTime.now(ZoneOffset.UTC),
                        hashes = json.hashes + (hashFormat to calculated)
                    )
                    FileSystem.SYSTEM.write(jsonFile.toOkioPath()) {
                        Json.encodeToStream(json, outputStream())
                    }

                    calculated
                } else {
                    null
                }
            }

            if (output.exists() && hash == curHash) {
                output
            } else {
                if (!output.parent.exists()) {
                    output.parent.createDirectories()
                }

                val calculated = download(output, downloadUrl, hash, hashFormat)

                if (calculated != hash) {
                    log.warn("Expected mod hash differs from actual hash. Expected: $hash, actual: $calculated")
                }

                // write new metadata
                json = json.copy(
                    lastAccess = ZonedDateTime.now(ZoneOffset.UTC),
                    hashes = json.hashes + (hashFormat to calculated)
                )
                FileSystem.SYSTEM.write(jsonFile.toOkioPath()) {
                    Json.encodeToStream(json, outputStream())
                }

                output
            }
        }

    private suspend fun download(to: Path, from: String, hash: String, hashFormat: HashFormat): String =
        withContext(Dispatchers.IO) {
            var calculatedHash = ""
            var attempts = 0

            do {
                if (attempts++ > MAX_DOWNLOAD_ATTEMPTS) {
                    throw IOException("Maximum download attempts reached. Unable to download $from")
                }

                if (attempts > 1) {
                    log.warn("Download attempt #${attempts - 1} failed. Retrying...")
                }

                try {
                    val response: HttpResponse = HTTP_CLIENT.get(from)

                    val fileSink = FileSystem.SYSTEM.sink(to.toOkioPath())
                    val hashSink = hashFormat.makeSink(fileSink)
                    hashSink.buffer().use { response.content.copyTo(it.outputStream()) }

                    calculatedHash = hashSink.hashString()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error("Error while downloading '$from'", e)
                }
            } while (hash != calculatedHash)

            calculatedHash
        }
}