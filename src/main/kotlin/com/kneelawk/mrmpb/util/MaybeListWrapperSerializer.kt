package com.kneelawk.mrmpb.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray

abstract class MaybeListWrapperSerializer<W : Any, E> : KSerializer<W> {
    abstract val elementSerializer: KSerializer<E>

    abstract fun new(elements: List<E>): W
    abstract fun get(wrapper: W): List<E>

    override fun deserialize(decoder: Decoder): W {
        val input = decoder.asJsonDecoder()

        val list = when (val element = input.decodeJsonElement()) {
            is JsonArray -> element.map { input.json.decodeFromJsonElement(elementSerializer, it) }
            else -> listOf(input.json.decodeFromJsonElement(elementSerializer, element))
        }

        return new(list)
    }

    override fun serialize(encoder: Encoder, value: W) {
        val output = encoder.asJsonEncoder()
        val elements = get(value)

        if (elements.size == 1) {
            output.encodeSerializableValue(elementSerializer, elements.first())
        } else {
            val array = buildJsonArray {
                for (element in elements) {
                    add(output.json.encodeToJsonElement(elementSerializer, element))
                }
            }
            output.encodeJsonElement(array)
        }
    }
}
