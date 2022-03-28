package com.kneelawk.mrmpb.model

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.mrmpb.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.mrmpb.model.manifest.quilt.QuiltLoaderJson
import com.kneelawk.mrmpb.net.manifest.ManifestCaches
import com.kneelawk.mrmpb.util.Either
import com.kneelawk.mrmpb.util.suspendGet
import com.kneelawk.mrmpb.model.manifest.forge.LoaderJson as ForgeLoaderJson


sealed class LoaderVersion {
    companion object {
        private fun fromFabricJson(json: FabricLoaderJson): Fabric {
            return Fabric(json.version)
        }

        private fun fromForgeJson(json: ForgeLoaderJson): Forge {
            val version = json.id
            return if (version.contains("-")) {
                Forge(json.id.split("-")[1])
            } else {
                Forge(version)
            }
        }

        private fun fromQuiltJson(json: QuiltLoaderJson): Quilt {
            return Quilt(json.version)
        }

        private val fabricLoaderListCache = Caffeine.newBuilder().buildAsync<Unit, List<Fabric>>()
        private val fabricLoaderMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, Fabric>>()
        private val forgeListMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, List<Forge>>>()
        private val forgeFullListCache = Caffeine.newBuilder().buildAsync<Unit, List<Forge>>()
        private val forgeFullMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, Forge>>()
        private val forgeListCache = Caffeine.newBuilder().buildAsync<String, List<Forge>>()
        private val forgeMapCache = Caffeine.newBuilder().buildAsync<String, Map<String, Forge>>()
        private val quiltLoaderListCache = Caffeine.newBuilder().buildAsync<Unit, List<Quilt>>()
        private val quiltLoaderMapCache = Caffeine.newBuilder().buildAsync<Unit, Map<String, Quilt>>()

        suspend fun fabricLoaderList(): List<Fabric> = fabricLoaderListCache.suspendGet(Unit) {
            ManifestCaches.fabricLoaders().map { fromFabricJson(it) }
        }

        private suspend fun fabricLoaderMap(): Map<String, Fabric> = fabricLoaderMapCache.suspendGet(Unit) {
            fabricLoaderList().associateBy { it.version }
        }

        suspend fun quiltLoaderList(): List<Quilt> = quiltLoaderListCache.suspendGet(Unit) {
            ManifestCaches.quiltManifest().map { fromQuiltJson(it) }
        }

        private suspend fun quiltLoaderMap(): Map<String, Quilt> = quiltLoaderMapCache.suspendGet(Unit) {
            quiltLoaderList().associateBy { it.version }
        }

        private suspend fun forgeListMap(): Map<String, List<Forge>> = forgeListMapCache.suspendGet(Unit) {
            ManifestCaches.forgeManifest().gameVersions.associate { gv ->
                gv.id to gv.loaders.map { fromForgeJson(it) }
            }
        }

        private suspend fun forgeFullList(): List<Forge> = forgeFullListCache.suspendGet(Unit) {
            ManifestCaches.forgeManifest().gameVersions.flatMap { gv -> gv.loaders.map { fromForgeJson(it) } }
        }

        private suspend fun forgeFullMap(): Map<String, Forge> = forgeFullMapCache.suspendGet(Unit) {
            forgeFullList().associateBy { it.version }
        }

        suspend fun forgeList(minecraftVersion: String?): List<Forge> {
            return if (minecraftVersion != null) {
                if (forgeListMap().containsKey(minecraftVersion)) {
                    forgeListCache.suspendGet(minecraftVersion) {
                        forgeListMap()[minecraftVersion]!!
                    }
                } else {
                    emptyList()
                }
            } else {
                forgeFullList()
            }
        }

        private suspend fun forgeMap(minecraftVersion: String?): Map<String, Forge> {
            return if (minecraftVersion != null) {
                if (forgeListMap().containsKey(minecraftVersion)) {
                    forgeMapCache.suspendGet(minecraftVersion) {
                        forgeListMap()[minecraftVersion]!!.associateBy { it.version }
                    }
                } else {
                    emptyMap()
                }
            } else {
                forgeFullMap()
            }
        }

        suspend fun forVersion(
            loaderVersion: String, minecraftVersion: String?
        ): Either<LoaderVersion, InvalidLoaderVersionError> {
            val lowerCase = loaderVersion.lowercase()
            return when {
                lowerCase.startsWith("fabric") -> {
                    val version = loaderVersion.substring("fabric".length).trim()
                    Either.leftOr(fabricLoaderMap()[version], InvalidLoaderVersionError.Fabric(version))
                }
                lowerCase.startsWith("quilt") -> {
                    val version = loaderVersion.substring("quilt".length).trim()
                    Either.leftOr(quiltLoaderMap()[version], InvalidLoaderVersionError.Quilt(version))
                }
                lowerCase.startsWith("forge") -> {
                    val version = loaderVersion.substring("forge".length).trim()
                    Either.leftOr(
                        forgeMap(minecraftVersion)[version], InvalidLoaderVersionError.Forge(version, minecraftVersion)
                    )
                }
                else -> Either.right(InvalidLoaderVersionError.UnknownLoader)
            }
        }
    }

    class Fabric internal constructor(val version: String) : LoaderVersion() {
        override fun toString(): String {
            return "Fabric $version"
        }
    }

    class Forge internal constructor(val version: String) : LoaderVersion() {
        override fun toString(): String {
            return "Forge $version"
        }
    }

    class Quilt internal constructor(val version: String) : LoaderVersion() {
        override fun toString(): String {
            return "Quilt $version"
        }
    }
}

sealed class InvalidLoaderVersionError {
    data class Fabric(val loaderVersion: String) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "'$loaderVersion' is not a real Fabric version"
        }
    }

    data class Forge(val loaderVersion: String, val minecraftVersion: String?) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return if (minecraftVersion == null) {
                "'$loaderVersion' is not a real Forge version"
            } else {
                "'$loaderVersion' is not a real Forge version for Minecraft '$minecraftVersion'"
            }
        }
    }

    data class Quilt(val loaderVersion: String) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "'$loaderVersion' is not a real Quilt version"
        }
    }

    object UnknownLoader : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "Loader strings must start with either 'Fabric' or 'Forge'"
        }
    }
}
