package com.kneelawk.mrmpb.engine.hash

import okio.Buffer
import okio.IOException
import okio.Source
import org.apache.commons.codec.digest.MurmurHash2

// Credit to https://github.com/packwiz/packwiz-installer/blob/master/src/main/kotlin/link/infra/packwiz/installer/metadata/hash/Murmur2Hasher.kt
class CursedMurmur2HashingSource(source: Source) : StringHashingSource(source) {
    private val internalBuffer = Buffer()
    private val tempBuffer = Buffer()

    fun hash(): Int {
        val data = internalBuffer.readByteArray()
        return MurmurHash2.hash32(data, data.size, 1)
    }

    override fun hashString(): String {
        return hash().toString()
    }

    @Throws(IOException::class)
    override fun read(sink: Buffer, byteCount: Long): Long {
        val out = delegate.read(tempBuffer, byteCount)
        if (out > -1) {
            sink.write(tempBuffer.clone(), out)
            computeNormalizedBufferFaster(tempBuffer, internalBuffer)
        }
        return out
    }

    private fun computeNormalizedBufferFaster(input: Buffer, output: Buffer) {
        var index = 0
        val arr = input.readByteArray()
        for (b in arr) {
            when (b) {
                9.toByte(), 10.toByte(), 13.toByte(), 32.toByte() -> {}
                else -> {
                    arr[index] = b
                    index++
                }
            }
        }
        output.write(arr, 0, index)
    }
}