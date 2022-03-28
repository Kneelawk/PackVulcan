package com.kneelawk.mrmpb.net.manifest

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.mrmpb.model.manifest.forge.ForgeManifestJson
import com.kneelawk.mrmpb.model.manifest.minecraft.MinecraftManifestJson
import com.kneelawk.mrmpb.util.suspendGet
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
object ManifestCaches {
    private val fabricLoadersCache = Caffeine.newBuilder().buildAsync<Unit, List<FabricLoaderJson>>()
    private val forgeManifestCache = Caffeine.newBuilder().buildAsync<Unit, ForgeManifestJson>()
    private val minecraftManifestCache = Caffeine.newBuilder().buildAsync<Unit, MinecraftManifestJson>()

    suspend fun fabricLoaders(): List<FabricLoaderJson> = fabricLoadersCache.suspendGet(Unit) {
        ManifestApis.fabricLoaders()
    }

    suspend fun forgeManifest(): ForgeManifestJson = forgeManifestCache.suspendGet(Unit) {
        ManifestApis.forgeManifest()
    }

    suspend fun minecraftManifest(): MinecraftManifestJson = minecraftManifestCache.suspendGet(Unit) {
        ManifestApis.minecraftManifest()
    }
}