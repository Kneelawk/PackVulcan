package com.kneelawk.mrmpb.model

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.manifest.minecraft.TypeJson
import com.kneelawk.mrmpb.net.manifest.ManifestCaches
import com.kneelawk.mrmpb.util.Either
import com.kneelawk.mrmpb.util.suspendGet
import com.kneelawk.mrmpb.model.manifest.minecraft.VersionJson as MinecraftVersionJson

class MinecraftVersion private constructor(val version: String, val type: Type) {
    companion object {
        private fun fromMinecraftJson(json: MinecraftVersionJson): MinecraftVersion {
            return MinecraftVersion(json.id, Type.fromJson(json.type))
        }

        private val minecraftVersionListCache = Caffeine.newBuilder().buildAsync<Unit, List<MinecraftVersion>>()
        private val minecraftVersionMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, MinecraftVersion>>()

        suspend fun minecraftVersionList(): List<MinecraftVersion> = minecraftVersionListCache.suspendGet(Unit) {
            ManifestCaches.minecraftManifest().versions.map { fromMinecraftJson(it) }
        }

        private suspend fun minecraftVersionMap(): Map<String, MinecraftVersion> =
            minecraftVersionMapCache.suspendGet(Unit) {
                minecraftVersionList().associateBy { it.version }
            }

        suspend fun forVersion(version: String): Either<MinecraftVersion, InvalidMinecraftVersionError> {
            return Either.leftOr(minecraftVersionMap()[version], InvalidMinecraftVersionError(version))
        }
    }

    override fun toString(): String {
        return version
    }

    enum class Type {
        OLD_ALPHA,
        OLD_BETA,
        RELEASE,
        SNAPSHOT;

        companion object {
            fun fromJson(typeJson: TypeJson): Type {
                return when (typeJson) {
                    TypeJson.OLD_ALPHA -> OLD_ALPHA
                    TypeJson.OLD_BETA -> OLD_BETA
                    TypeJson.RELEASE -> RELEASE
                    TypeJson.SNAPSHOT -> SNAPSHOT
                }
            }
        }
    }
}

data class InvalidMinecraftVersionError(val givenVersion: String) {
    override fun toString(): String {
        return "'$givenVersion' is not a real Minecraft version"
    }
}
