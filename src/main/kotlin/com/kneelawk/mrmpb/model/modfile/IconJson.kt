package com.kneelawk.mrmpb.model.modfile

import com.kneelawk.mrmpb.util.asJsonDecoder
import com.kneelawk.mrmpb.util.asJsonEncoder
import com.kneelawk.mrmpb.util.decodeMap
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.mapSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.json.*

@Serializable(IconSerializer::class)
sealed class IconJson {
    data class Single(val path: String) : IconJson()
    data class Multiple(val paths: Map<String, String>) : IconJson()
}

object IconSerializer : JsonContentPolymorphicSerializer<IconJson>(IconJson::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out IconJson> {
        return when (element) {
            is JsonObject -> Multiple
            else -> Single
        }
    }

    private object Single : KSerializer<IconJson.Single> {
        override val descriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.mrmpb.model.modfile.fabric.IconJson.Single", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): IconJson.Single {
            return IconJson.Single(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: IconJson.Single) {
            encoder.encodeString(value.path)
        }
    }

    private object Multiple : KSerializer<IconJson.Multiple> {
        private val keyDescriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.mrmpb.model.modfile.fabric.IconJson.Multiple.key", PrimitiveKind.STRING
        )
        private val valueDescriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.mrmpb.model.modfile.fabric.IconJson.Multiple.value", PrimitiveKind.STRING
        )

        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor = mapSerialDescriptor(keyDescriptor, valueDescriptor)

        override fun deserialize(decoder: Decoder): IconJson.Multiple {
            val input = decoder.asJsonDecoder()
            val obj = input.decodeJsonElement().jsonObject
            val map = obj.mapValues { (_, value) -> value.jsonPrimitive.content }
            return IconJson.Multiple(map)
        }

        override fun serialize(encoder: Encoder, value: IconJson.Multiple) {
            val output = encoder.asJsonEncoder()
            val obj = buildJsonObject {
                for ((key, path) in value.paths) {
                    put(key, path)
                }
            }
            output.encodeJsonElement(obj)
        }
    }
}
