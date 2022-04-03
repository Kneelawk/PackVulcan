package com.kneelawk.mrmpb.net.manifest

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.mrmpb.model.manifest.forge.ForgeManifestJson
import com.kneelawk.mrmpb.model.manifest.minecraft.MinecraftManifestJson
import com.kneelawk.mrmpb.model.manifest.quilt.QuiltLoaderJson
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import com.kneelawk.mrmpb.util.suspendGet
import io.ktor.client.request.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(DelicateCoroutinesApi::class)
object ManifestApis {
    private val fabricLoadersCache = Caffeine.newBuilder().buildAsync<Unit, List<FabricLoaderJson>>()
    private val forgeManifestCache = Caffeine.newBuilder().buildAsync<Unit, ForgeManifestJson>()
    private val minecraftManifestCache = Caffeine.newBuilder().buildAsync<Unit, MinecraftManifestJson>()
    private val quiltManifestCache = Caffeine.newBuilder().buildAsync<Unit, List<QuiltLoaderJson>>()

    /*
     * Retriever methods.
     */

    private suspend fun retrieveFabricLoaders(): List<FabricLoaderJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.fabricmc.net/v2/versions/loader")
    }

    private suspend fun retrieveForgeManifest(): ForgeManifestJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/forge/v0/manifest.json")
    }

    private suspend fun retrieveMinecraftManifest(): MinecraftManifestJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/minecraft/v0/manifest.json")
    }

    private suspend fun retrieveQuiltManifest(): List<QuiltLoaderJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.quiltmc.org/v3/versions/loader")
    }

    /*
     * Accessor methods.
     */

    suspend fun fabricLoaders(): List<FabricLoaderJson> = fabricLoadersCache.suspendGet(Unit) {
        retrieveFabricLoaders()
    }

    suspend fun forgeManifest(): ForgeManifestJson = forgeManifestCache.suspendGet(Unit) {
        retrieveForgeManifest()
    }

    suspend fun minecraftManifest(): MinecraftManifestJson = minecraftManifestCache.suspendGet(Unit) {
        retrieveMinecraftManifest()
    }

    suspend fun quiltManifest(): List<QuiltLoaderJson> = quiltManifestCache.suspendGet(Unit) {
        retrieveQuiltManifest()
    }
}