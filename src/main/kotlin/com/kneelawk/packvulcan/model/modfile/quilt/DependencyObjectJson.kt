package com.kneelawk.packvulcan.model.modfile.quilt

import com.kneelawk.packvulcan.model.modfile.StringOrArrayJson
import com.kneelawk.packvulcan.model.modfile.StringOrArraySerializer
import com.kneelawk.packvulcan.util.MaybeListWrapperSerializer
import com.kneelawk.packvulcan.util.asJsonDecoder
import com.kneelawk.packvulcan.util.asJsonEncoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(DependencyObjectListSerializer::class)
data class DependencyObjectListJson(val objs: List<DependencyObjectJson>) : List<DependencyObjectJson> by objs

private val DEFAULT_VERSIONS = StringOrArrayJson(listOf("*"))

@Serializable(DependencyObjectSerializer::class)
sealed class DependencyObjectJson {
    data class Identifier(val identifier: String) : DependencyObjectJson()

    @Serializable(DependencyObjectObjectSerializer::class)
    data class Object(
        val id: String,
        val versions: StringOrArrayJson = DEFAULT_VERSIONS,
        val reason: String? = null,
        val optional: Boolean = false,
        val unless: DependencyObjectListJson? = null,
        val otherFields: JsonObject,
    ) : DependencyObjectJson()
}

object DependencyObjectObjectSerializer : KSerializer<DependencyObjectJson.Object> {
    override val descriptor =
        buildClassSerialDescriptor("com.kneelawk.packvulcan.model.modfile.quilt.DependencyObjectJson.Object") {
            element<String>("id")
            element<String>("versions", isOptional = true)
            element<String>("reason", isOptional = true)
            element<DependencyObjectListJson>("unless", isOptional = true)
            element<JsonObject>("otherFields")
        }

    override fun deserialize(decoder: Decoder): DependencyObjectJson.Object {
        val input = decoder.asJsonDecoder()
        val obj = input.decodeJsonElement().jsonObject

        val id = obj.getValue("id").jsonPrimitive.content
        val versions =
            obj["versions"]?.let { input.json.decodeFromJsonElement(StringOrArraySerializer, it) } ?: DEFAULT_VERSIONS
        val reason = obj["reason"]?.jsonPrimitive?.content
        val optional = obj["optional"]?.jsonPrimitive?.booleanOrNull ?: false
        val unless = obj["unless"]?.let { input.json.decodeFromJsonElement(DependencyObjectListSerializer, it) }

        val otherFields = obj.toMutableMap()
        otherFields.remove("id")
        otherFields.remove("versions")
        otherFields.remove("reason")
        otherFields.remove("optional")
        otherFields.remove("unless")

        return DependencyObjectJson.Object(id, versions, reason, optional, unless, JsonObject(otherFields))
    }

    override fun serialize(encoder: Encoder, value: DependencyObjectJson.Object) {
        val output = encoder.asJsonEncoder()
        val obj = buildJsonObject {
            put("id", value.id)
            if (value.versions != DEFAULT_VERSIONS) {
                put("versions", output.json.encodeToJsonElement(StringOrArraySerializer, value.versions))
            }
            value.reason?.let { put("reason", it) }
            if (value.optional) {
                put("optional", true)
            }
            value.unless?.let { put("unless", output.json.encodeToJsonElement(DependencyObjectListSerializer, it)) }

            for ((key, element) in value.otherFields) {
                put(key, element)
            }
        }
        output.encodeJsonElement(obj)
    }
}

object DependencyObjectSerializer :
    JsonContentPolymorphicSerializer<DependencyObjectJson>(DependencyObjectJson::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out DependencyObjectJson> {
        return when (element) {
            is JsonObject -> DependencyObjectObjectSerializer
            else -> Identifier
        }
    }

    private object Identifier : KSerializer<DependencyObjectJson.Identifier> {
        override val descriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.packvulcan.model.packvulcan.quilt.DependencyObjectJson.Identifier", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): DependencyObjectJson.Identifier =
            DependencyObjectJson.Identifier(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: DependencyObjectJson.Identifier) =
            encoder.encodeString(value.identifier)
    }
}

object DependencyObjectListSerializer : MaybeListWrapperSerializer<DependencyObjectListJson, DependencyObjectJson>() {
    override val elementSerializer = DependencyObjectSerializer

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor =
        buildClassSerialDescriptor("com.kneelawk.packvulcan.model.modfile.quilt.DependencyObjectListJson") {
            element("objs", listSerialDescriptor(elementSerializer.descriptor))
        }

    override fun new(elements: List<DependencyObjectJson>): DependencyObjectListJson =
        DependencyObjectListJson(elements)

    override fun get(wrapper: DependencyObjectListJson): List<DependencyObjectJson> = wrapper.objs
}
