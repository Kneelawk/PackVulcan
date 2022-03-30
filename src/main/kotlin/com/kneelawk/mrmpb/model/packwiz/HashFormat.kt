package com.kneelawk.mrmpb.model.packwiz

import com.kneelawk.mrmpb.engine.hash.*
import okio.HashingSink
import okio.HashingSource
import okio.Sink
import okio.Source

enum class HashFormat {
    MD5,
    MURMUR2,
    SHA1,
    SHA256,
    SHA512;

    companion object {
        @Throws(LoadError::class)
        fun fromString(string: String): HashFormat {
            return when (string.lowercase()) {
                "md5" -> MD5
                "murmur2" -> MURMUR2
                "sha1" -> SHA1
                "sha256" -> SHA256
                "sha512" -> SHA512
                else -> throw LoadError.BadHashFormat(string)
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            MD5 -> "md5"
            MURMUR2 -> "murmur2"
            SHA1 -> "sha1"
            SHA256 -> "sha256"
            SHA512 -> "sha512"
        }
    }

    fun makeSource(source: Source): StringHashingSource {
        return when (this) {
            MD5 -> WrapperHashingSource(HashingSource.md5(source))
            MURMUR2 -> CursedMurmur2HashingSource(source)
            SHA1 -> WrapperHashingSource(HashingSource.sha1(source))
            SHA256 -> WrapperHashingSource(HashingSource.sha256(source))
            SHA512 -> WrapperHashingSource(HashingSource.sha512(source))
        }
    }

    fun makeSink(sink: Sink): StringHashingSink {
        return when (this) {
            MD5 -> WrapperHashingSink(HashingSink.md5(sink))
            MURMUR2 -> CursedMurmur2HashingSink(sink)
            SHA1 -> WrapperHashingSink(HashingSink.sha1(sink))
            SHA256 -> WrapperHashingSink(HashingSink.sha256(sink))
            SHA512 -> WrapperHashingSink(HashingSink.sha512(sink))
        }
    }
}