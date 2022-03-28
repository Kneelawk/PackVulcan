package com.kneelawk.mrmpb.net.manifest

import com.kneelawk.mrmpb.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.mrmpb.model.manifest.forge.ForgeManifestJson
import com.kneelawk.mrmpb.model.manifest.minecraft.MinecraftManifestJson
import com.kneelawk.mrmpb.net.HTTP_CLIENT
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ManifestApis {
    suspend fun fabricLoaders(): List<FabricLoaderJson> {
        return withContext(Dispatchers.IO) {
            HTTP_CLIENT.get("https://meta.fabricmc.net/v2/versions/loader")
        }
    }

    suspend fun forgeManifest(): ForgeManifestJson {
        return withContext(Dispatchers.IO) {
            HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/forge/v0/manifest.json")
        }
    }

    suspend fun minecraftManifest(): MinecraftManifestJson {
        return withContext(Dispatchers.IO) {
            HTTP_CLIENT.get("https://meta.modrinth.com/gamedata/minecraft/v0/manifest.json")
        }
    }
}