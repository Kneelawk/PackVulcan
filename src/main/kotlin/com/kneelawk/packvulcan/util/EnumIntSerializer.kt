package com.kneelawk.packvulcan.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

abstract class EnumIntSerializer<E : Enum<E>>(serialName: String, enumClass: KClass<E>) :
    KSerializer<E> {
    private val values = enumClass.java.enumConstants
    private val ordinalArray: IntArray =
        values.map { it.declaringJavaClass.getField(it.name).getAnnotation(SerialInt::class.java)?.value ?: it.ordinal }
            .toIntArray()
    private val reverseMap = mutableMapOf<Int, E>()

    init {
        for (value in values) {
            val index = value.ordinal
            val ordinal = ordinalArray[index]

            if (reverseMap.containsKey(ordinal)) throw IllegalArgumentException(
                "Both ${reverseMap[ordinal]} and $value have ordinal $ordinal"
            )

            reverseMap[ordinal] = value
        }
    }

    override val descriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: E) {
        val index = value.ordinal
        encoder.encodeInt(ordinalArray[index])
    }

    override fun deserialize(decoder: Decoder): E {
        val ordinal = decoder.decodeInt()
        return reverseMap[ordinal] ?: values[0]
    }
}

annotation class SerialInt(val value: Int)
