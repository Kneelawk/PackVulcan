package com.kneelawk.packvulcan.model.modfile.quilt

import com.kneelawk.packvulcan.util.MaybeListWrapperSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(LicenseListSerializer::class)
data class LicenseListJson(val licenses: List<LicenseJson>) : List<LicenseJson> by licenses

@Serializable(LicenseSerializer::class)
sealed class LicenseJson {
    data class Identifier(val identifier: String) : LicenseJson()

    @Serializable
    data class Object(
        val name: String,
        val id: String,
        val url: String,
        val description: String? = null,
    ) : LicenseJson()
}

object LicenseSerializer : JsonContentPolymorphicSerializer<LicenseJson>(LicenseJson::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out LicenseJson> {
        return when (element) {
            is JsonObject -> LicenseJson.Object.serializer()
            else -> Identifier
        }
    }

    private object Identifier : KSerializer<LicenseJson.Identifier> {
        override val descriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.packvulcan.model.modfile.quilt.LicenseJson.Identifier", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): LicenseJson.Identifier {
            return LicenseJson.Identifier(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: LicenseJson.Identifier) {
            encoder.encodeString(value.identifier)
        }
    }
}

object LicenseListSerializer : MaybeListWrapperSerializer<LicenseListJson, LicenseJson>() {
    override val elementSerializer = LicenseSerializer

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = buildClassSerialDescriptor("com.kneelawk.packvulcan.model.modfile.quilt.LicenseListJson") {
        element("licenses", listSerialDescriptor(elementSerializer.descriptor))
    }

    override fun new(elements: List<LicenseJson>): LicenseListJson = LicenseListJson(elements)

    override fun get(wrapper: LicenseListJson): List<LicenseJson> = wrapper.licenses
}
