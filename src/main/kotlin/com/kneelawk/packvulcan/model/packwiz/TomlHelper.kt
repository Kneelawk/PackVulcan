package com.kneelawk.packvulcan.model.packwiz

import com.kneelawk.packvulcan.model.HashFormat
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Sink
import okio.Source
import okio.blackholeSink
import okio.buffer
import java.io.IOException

object TomlHelper {
    // TomlWriter is thread-safe, so might as well use the same one.
    private val writer = TomlWriter()

    @Throws(LoadError::class, IOException::class, IllegalStateException::class)
    suspend fun <T> read(decoder: FromToml<T>, source: Source): T = withContext(Dispatchers.IO) {
        val toml = Toml()
        source.buffer().use { toml.read(it.inputStream()) }
        decoder.fromToml(toml)
    }

    @Throws(LoadError::class, IllegalStateException::class)
    suspend fun <T> read(decoder: FromToml<T>, string: String): T = withContext(Dispatchers.IO) {
        val toml = Toml()
        toml.read(string)
        decoder.fromToml(toml)
    }

    @Throws(IOException::class)
    suspend fun write(encodable: ToToml, sink: Sink) = withContext(Dispatchers.IO) {
        sink.buffer().use { writer.write(encodable.toToml(), it.outputStream()) }
    }

    suspend fun writeToString(encodable: ToToml): String = withContext(Dispatchers.IO) {
        writer.write(encodable.toToml())
    }

    suspend fun hash(encodable: ToToml, hashFormat: HashFormat): String = withContext(Dispatchers.IO) {
        val hasher = hashFormat.makeSink(blackholeSink())
        hasher.buffer().use { writer.write(encodable.toToml(), it.outputStream()) }
        hasher.hashString()
    }
}