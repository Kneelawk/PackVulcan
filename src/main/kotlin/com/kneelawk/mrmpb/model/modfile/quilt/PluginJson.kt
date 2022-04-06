package com.kneelawk.mrmpb.model.modfile.quilt

import com.kneelawk.mrmpb.util.AdaptableSerializer
import kotlinx.serialization.Serializable

@Serializable(PluginSerializer::class)
data class PluginJson(val value: String, val adapter: String = "default")

object PluginSerializer : AdaptableSerializer<PluginJson>() {
    override val serialName = "com.kneelawk.mrmpb.model.modfile.quilt.PluginJson"

    override fun new(value: String, adapter: String): PluginJson = PluginJson(value, adapter)

    override fun getValue(t: PluginJson): String = t.value

    override fun getAdapter(t: PluginJson): String = t.adapter
}
