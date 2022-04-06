package com.kneelawk.mrmpb.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder

@OptIn(ExperimentalSerializationApi::class)
fun <T> Decoder.decodeList(listDescriptor: SerialDescriptor, elementDecoder: CompositeDecoder.(Int) -> T): List<T> {
    val composite = beginStructure(listDescriptor)
    val list = mutableListOf<T>()

    if (composite.decodeSequentially()) {
        val size = composite.decodeCollectionSize(listDescriptor)
        for (index in 0 until size) {
            list.add(index, composite.elementDecoder(index))
        }
    } else {
        while (true) {
            val index = composite.decodeElementIndex(listDescriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            list.add(index, composite.elementDecoder(index))
        }
    }

    composite.endStructure(listDescriptor)

    return list
}

@OptIn(ExperimentalSerializationApi::class)
fun <K, V> Decoder.decodeMap(
    mapDescriptor: SerialDescriptor, step: Int = 2, entryDecoder: CompositeDecoder.(Int, Boolean) -> Pair<K, V>
): Map<K, V> {
    val composite = beginStructure(mapDescriptor)
    val map = mutableMapOf<K, V>()

    if (composite.decodeSequentially()) {
        val size = composite.decodeCollectionSize(mapDescriptor)
        for (index in 0 until size step step) {
            map += composite.entryDecoder(index, true)
        }
    } else {
        while (true) {
            val index = composite.decodeElementIndex(mapDescriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            map += composite.entryDecoder(index, false)
        }
    }

    composite.endStructure(mapDescriptor)

    return map
}

internal fun Decoder.asJsonDecoder(): JsonDecoder = this as? JsonDecoder
    ?: throw IllegalStateException(
        "This serializer can be used only with Json format." +
                "Expected Decoder to be JsonDecoder, got ${this::class}"
    )

internal fun Encoder.asJsonEncoder() = this as? JsonEncoder
    ?: throw IllegalStateException(
        "This serializer can be used only with Json format." +
                "Expected Encoder to be JsonEncoder, got ${this::class}"
    )
