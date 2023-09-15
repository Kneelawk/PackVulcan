package com.kneelawk.packvulcan.model.modfile.quilt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltLoaderJson(
    val group: String,
    val id: String,
    val provides: List<ProvidesJson>? = null,
    val version: String,
    val entrypoints: Map<String, EntrypointListJson>? = null,
    val plugins: List<PluginJson>? = null,
    val jars: List<String>? = null,
    @SerialName("language_adapters")
    val languageAdapters: Map<String, String>? = null,
//    val depends: List<DependencyObjectListJson>? = null,
//    val breaks: List<DependencyObjectListJson>? = null,
    @SerialName("load_type")
    val loadType: LoadTypeJson? = null,
    val repositories: List<String>? = null,
    val metadata: MetadataJson? = null,
)
