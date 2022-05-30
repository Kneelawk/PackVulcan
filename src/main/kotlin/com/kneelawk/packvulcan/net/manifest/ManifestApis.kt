package com.kneelawk.packvulcan.net.manifest

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.packvulcan.model.manifest.forge.ForgeManifestJson
import com.kneelawk.packvulcan.model.manifest.minecraft.MinecraftManifestJson
import com.kneelawk.packvulcan.model.manifest.quilt.QuiltLoaderJson
import com.kneelawk.packvulcan.net.HTTP_CLIENT
import com.kneelawk.packvulcan.util.suspendGet
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ManifestApis {
    private val fabricLoadersCache = Caffeine.newBuilder().buildAsync<Unit, List<FabricLoaderJson>>()
    private val forgeManifestCache = Caffeine.newBuilder().buildAsync<Unit, ForgeManifestJson>()
    private val minecraftManifestCache = Caffeine.newBuilder().buildAsync<Unit, MinecraftManifestJson>()
    private val quiltManifestCache = Caffeine.newBuilder().buildAsync<Unit, List<QuiltLoaderJson>>()

    /*
     * Retriever methods.
     */

    private suspend fun retrieveFabricLoaders(): List<FabricLoaderJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.fabricmc.net/v2/versions/loader").body()
    }

    private suspend fun retrieveForgeManifest(): ForgeManifestJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/forge/v0/manifest.json").body()
    }

    private suspend fun retrieveMinecraftManifest(): MinecraftManifestJson = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/minecraft/v0/manifest.json").body()
    }

    private suspend fun retrieveQuiltManifest(): List<QuiltLoaderJson> = withContext(Dispatchers.IO) {
        HTTP_CLIENT.get("https://meta.quiltmc.org/v3/versions/loader").body()
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
