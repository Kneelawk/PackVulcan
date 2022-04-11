package com.kneelawk.packvulcan.model

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.manifest.minecraft.TypeJson
import com.kneelawk.packvulcan.net.manifest.ManifestApis
import com.kneelawk.packvulcan.util.Either
import com.kneelawk.packvulcan.util.leftOr
import com.kneelawk.packvulcan.util.suspendGet
import com.kneelawk.packvulcan.model.manifest.minecraft.VersionJson as MinecraftVersionJson

class MinecraftVersion private constructor(val version: String, val type: Type) {
    companion object {
        val DEFAULT_VERSION = MinecraftVersion("1.18.2", Type.RELEASE)

        private fun fromMinecraftJson(json: MinecraftVersionJson): MinecraftVersion {
            return MinecraftVersion(json.id, Type.fromJson(json.type))
        }

        private val minecraftVersionListCache = Caffeine.newBuilder().buildAsync<Unit, List<MinecraftVersion>>()
        private val minecraftVersionMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, MinecraftVersion>>()

        suspend fun minecraftVersionList(): List<MinecraftVersion> = minecraftVersionListCache.suspendGet(Unit) {
            ManifestApis.minecraftManifest().versions.map { fromMinecraftJson(it) }
        }

        private suspend fun minecraftVersionMap(): Map<String, MinecraftVersion> =
            minecraftVersionMapCache.suspendGet(Unit) {
                minecraftVersionList().associateBy { it.version }
            }

        suspend fun forVersion(version: String): Either<MinecraftVersion, InvalidMinecraftVersionError> {
            return leftOr(minecraftVersionMap()[version], InvalidMinecraftVersionError(version))
        }
    }

    override fun toString(): String {
        return version
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinecraftVersion

        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        return version.hashCode()
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
        return "'$givenVersion' is not a real Minecraft version."
    }
}
