package com.kneelawk.mrmpb.model.modfile.fabric

import com.kneelawk.mrmpb.util.MaybeListWrapperSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor

@Serializable(EnvironmentSerializer::class)
data class EnvironmentListJson(val environments: List<EnvironmentJson>) : List<EnvironmentJson> by environments

@Serializable
enum class EnvironmentJson {
    @SerialName("*")
    ALL,

    @SerialName("client")
    CLIENT,

    @SerialName("server")
    SERVER;
}

object EnvironmentSerializer : MaybeListWrapperSerializer<EnvironmentListJson, EnvironmentJson>() {
    override val elementSerializer = EnvironmentJson.serializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = buildClassSerialDescriptor("com.kneelawk.mrmpb.model.modfile.fabric.EnvironmentJson") {
        element("environments", listSerialDescriptor(elementSerializer.descriptor))
    }

    override fun new(elements: List<EnvironmentJson>): EnvironmentListJson = EnvironmentListJson(elements)

    override fun get(wrapper: EnvironmentListJson): List<EnvironmentJson> = wrapper.environments
}