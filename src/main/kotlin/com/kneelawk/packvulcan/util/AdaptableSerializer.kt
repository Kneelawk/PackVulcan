package com.kneelawk.packvulcan.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

abstract class AdaptableSerializer<T> : KSerializer<T> {
    open val valueFieldName = "value"
    open val adapterFieldName = "adapter"

    open val valueElementName: String
        get() = valueFieldName
    open val adapterElementName: String
        get() = adapterFieldName

    open val adapterDefaultValue = "default"

    abstract val serialName: String

    abstract fun new(value: String, adapter: String): T
    abstract fun getValue(t: T): String
    abstract fun getAdapter(t: T): String

    override val descriptor by lazy {
        buildClassSerialDescriptor(serialName) {
            element<String>(valueFieldName)
            element<String>(adapterFieldName, isOptional = true)
        }
    }

    override fun deserialize(decoder: Decoder): T {
        val input = decoder.asJsonDecoder()
        return when (val element = input.decodeJsonElement()) {
            is JsonObject -> {
                val obj = element.jsonObject
                new(
                    obj.getValue(valueElementName).jsonPrimitive.content,
                    obj[adapterElementName]?.jsonPrimitive?.content ?: adapterDefaultValue
                )
            }
            else -> {
                new(element.jsonPrimitive.content, adapterDefaultValue)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: T) {
        val valueStr = getValue(value)
        val adapter = getAdapter(value)
        if (adapter == adapterDefaultValue) {
            encoder.encodeString(valueStr)
        } else {
            val output = encoder.asJsonEncoder()
            val obj = buildJsonObject {
                put(valueElementName, valueStr)
                put(adapterElementName, adapter)
            }
            output.encodeJsonElement(obj)
        }
    }
}
