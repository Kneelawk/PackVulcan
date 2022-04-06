package com.kneelawk.mrmpb.engine.hash

import com.kneelawk.mrmpb.model.HashFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Source
import okio.blackholeSink
import okio.buffer
import java.nio.file.Path

object HashHelper {
    suspend fun hash(source: Source, hashFormat: HashFormat): String = withContext(Dispatchers.IO) {
        val hasher = hashFormat.makeSink(blackholeSink())
        source.buffer().use { it.readAll(hasher) }
        hasher.hashString()
    }

    suspend fun hash(path: Path, hashFormat: HashFormat): String = withContext(Dispatchers.IO) {
        hash(FileSystem.SYSTEM.source(path.toOkioPath()), hashFormat)
    }
}