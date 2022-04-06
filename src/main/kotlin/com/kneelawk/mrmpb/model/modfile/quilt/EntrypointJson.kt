package com.kneelawk.mrmpb.model.modfile.quilt

import com.kneelawk.mrmpb.util.AdaptableSerializer
import com.kneelawk.mrmpb.util.MaybeListWrapperSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor

@Serializable(EntrypointListSerializer::class)
data class EntrypointListJson(val entrypoints: List<EntrypointJson>) : List<EntrypointJson> by entrypoints

@Serializable(EntrypointSerializer::class)
data class EntrypointJson(
    val value: String,
    val adapter: String = "default",
)

object EntrypointSerializer : AdaptableSerializer<EntrypointJson>() {
    override val serialName = "com.kneelawk.mrmpb.model.modfile.quilt.EntrypointJson"

    override fun new(value: String, adapter: String): EntrypointJson = EntrypointJson(value, adapter)

    override fun getValue(t: EntrypointJson): String = t.value

    override fun getAdapter(t: EntrypointJson): String = t.adapter
}

object EntrypointListSerializer : MaybeListWrapperSerializer<EntrypointListJson, EntrypointJson>() {
    override val elementSerializer = EntrypointSerializer

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = buildClassSerialDescriptor("com.kneelawk.mrmpb.model.modfile.quilt.EntrypointListJson") {
        element("entrypoints", listSerialDescriptor(elementSerializer.descriptor))
    }

    override fun new(elements: List<EntrypointJson>): EntrypointListJson = EntrypointListJson(elements)

    override fun get(wrapper: EntrypointListJson): List<EntrypointJson> = wrapper.entrypoints
}
