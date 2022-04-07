package com.kneelawk.packvulcan.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

abstract class MaybeListSerializer<T : Any, S : T, M : T, E>(baseClass: KClass<T>) :
    JsonContentPolymorphicSerializer<T>(baseClass) {
    private val single = Single()
    private val multiple = Multiple()

    abstract val elementSerializer: KSerializer<E>

    abstract fun newSingle(element: E): S
    abstract fun newMultiple(elements: List<E>): M

    abstract fun getElement(single: S): E
    abstract fun getElements(multiple: M): List<E>

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out T> {
        return when (element) {
            is JsonArray -> multiple
            else -> single
        }
    }

    private inner class Single : KSerializer<S> {
        override val descriptor = elementSerializer.descriptor

        override fun deserialize(decoder: Decoder): S = newSingle(decoder.decodeSerializableValue(elementSerializer))

        override fun serialize(encoder: Encoder, value: S) =
            encoder.encodeSerializableValue(elementSerializer, getElement(value))
    }

    private inner class Multiple : KSerializer<M> {
        private val elementDescriptor = elementSerializer.descriptor

        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor = listSerialDescriptor(elementDescriptor)

        override fun deserialize(decoder: Decoder): M {
            return newMultiple(
                decoder.decodeList(descriptor) { decodeSerializableElement(elementDescriptor, it, elementSerializer) })
        }

        override fun serialize(encoder: Encoder, value: M) {
            val elements = getElements(value)
            val size = elements.size
            encoder.encodeCollection(descriptor, size) {
                for (index in 0 until size) {
                    encodeSerializableElement(elementDescriptor, index, elementSerializer, elements[index])
                }
            }
        }
    }
}