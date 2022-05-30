package com.kneelawk.packvulcan.model.modfile.quilt

import com.kneelawk.packvulcan.util.asJsonDecoder
import com.kneelawk.packvulcan.util.asJsonEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(ProvidesSerializer::class)
data class ProvidesJson(
    val id: String,
    val version: String? = null,
)

object ProvidesSerializer : KSerializer<ProvidesJson> {
    override val descriptor = buildClassSerialDescriptor("com.kneelawk.packvulcan.model.modfile.quilt.ProvidesJson") {
        element<String>("id")
        element<String>("version", isOptional = true)
    }

    override fun deserialize(decoder: Decoder): ProvidesJson {
        val input = decoder.asJsonDecoder()
        val element = input.decodeJsonElement()
        return if (element is JsonObject) {
            ProvidesJson(element.getValue("id").jsonPrimitive.content, element["version"]?.jsonPrimitive?.content)
        } else {
            ProvidesJson(element.jsonPrimitive.content)
        }
    }

    override fun serialize(encoder: Encoder, value: ProvidesJson) {
        val output = encoder.asJsonEncoder()
        if (value.version != null) {
            output.encodeJsonElement(buildJsonObject {
                put("id", value.id)
                put("version", value.version)
            })
        } else {
            output.encodeString(value.id)
        }
    }
}
