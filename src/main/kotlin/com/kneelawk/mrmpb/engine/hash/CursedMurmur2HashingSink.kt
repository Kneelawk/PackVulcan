package com.kneelawk.mrmpb.engine.hash

import okio.Buffer
import okio.IOException
import okio.Sink
import org.apache.commons.codec.digest.MurmurHash2

class CursedMurmur2HashingSink(sink: Sink) : StringHashingSink(sink) {
    private val internalBuffer = Buffer()

    fun hash(): Int {
        val data = internalBuffer.readByteArray()
        return MurmurHash2.hash32(data, data.size, 1)
    }

    override fun hashString(): String {
        return hash().toString()
    }

    @Throws(IOException::class)
    override fun write(source: Buffer, byteCount: Long) {
        computeNormalizedBufferFaster(source.clone(), internalBuffer)
        delegate.write(source, byteCount)
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