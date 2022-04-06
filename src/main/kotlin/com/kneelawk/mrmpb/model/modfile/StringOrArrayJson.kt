package com.kneelawk.mrmpb.model.modfile

import com.kneelawk.mrmpb.util.MaybeListWrapperSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor

@Serializable(StringOrArraySerializer::class)
data class StringOrArrayJson(val array: List<String>) : List<String> by array

object StringOrArraySerializer : MaybeListWrapperSerializer<StringOrArrayJson, String>() {
    override val elementSerializer = String.serializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = buildClassSerialDescriptor("com.kneelawk.mrmpb.model.modfile.StringOrArrayJson") {
        element("strings", listSerialDescriptor(elementSerializer.descriptor))
    }

    override fun new(elements: List<String>): StringOrArrayJson = StringOrArrayJson(elements)

    override fun get(wrapper: StringOrArrayJson): List<String> = wrapper.array
}
