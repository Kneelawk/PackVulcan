package com.kneelawk.packvulcan.model.modfile.fabric

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(PersonSerializer::class)
sealed class PersonJson {
    abstract val name: String

    data class Simple(override val name: String) : PersonJson()

    @Serializable
    data class Contact(override val name: String, val contact: Map<String, String>? = null) : PersonJson()
}

private class PersonSerializer : JsonContentPolymorphicSerializer<PersonJson>(PersonJson::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<PersonJson> {
        return when (element) {
            is JsonObject -> PersonJson.Contact.serializer()
            else -> Simple
        }
    }

    private object Simple : KSerializer<PersonJson.Simple> {
        override val descriptor = PrimitiveSerialDescriptor(
            "com.kneelawk.packvulcan.model.modfile.fabric.PersonJson.Simple", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): PersonJson.Simple {
            return PersonJson.Simple(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: PersonJson.Simple) {
            encoder.encodeString(value.name)
        }
    }
}
