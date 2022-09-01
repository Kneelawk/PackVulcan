package com.kneelawk.packvulcan.model

import com.github.benmanes.caffeine.cache.Caffeine
import com.kneelawk.packvulcan.model.manifest.fabric.FabricLoaderJson
import com.kneelawk.packvulcan.model.manifest.quilt.QuiltLoaderJson
import com.kneelawk.packvulcan.net.manifest.ManifestApis
import com.kneelawk.packvulcan.util.Either
import com.kneelawk.packvulcan.util.leftOr
import com.kneelawk.packvulcan.util.right
import com.kneelawk.packvulcan.util.suspendGet
import com.kneelawk.packvulcan.model.manifest.forge.LoaderJson as ForgeLoaderJson


sealed class LoaderVersion {
    companion object {
        val DEFAULT_VERSION: LoaderVersion = Quilt("0.17.4")

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
            ManifestApis.fabricLoaders().map { fromFabricJson(it) }
        }

        private suspend fun fabricLoaderMap(): Map<String, Fabric> = fabricLoaderMapCache.suspendGet(Unit) {
            fabricLoaderList().associateBy { it.version }
        }

        suspend fun quiltLoaderList(): List<Quilt> = quiltLoaderListCache.suspendGet(Unit) {
            ManifestApis.quiltManifest().map { fromQuiltJson(it) }
        }

        private suspend fun quiltLoaderMap(): Map<String, Quilt> = quiltLoaderMapCache.suspendGet(Unit) {
            quiltLoaderList().associateBy { it.version }
        }

        private suspend fun forgeListMap(): Map<String, List<Forge>> = forgeListMapCache.suspendGet(Unit) {
            ManifestApis.forgeManifest().gameVersions.associate { gv ->
                gv.id to gv.loaders.map { fromForgeJson(it) }
            }
        }

        private suspend fun forgeFullList(): List<Forge> = forgeFullListCache.suspendGet(Unit) {
            ManifestApis.forgeManifest().gameVersions.flatMap { gv -> gv.loaders.map { fromForgeJson(it) } }
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
            return when (Type.forVersion(loaderVersion)) {
                Type.FABRIC -> {
                    val version = loaderVersion.substring("fabric".length).trim()
                    leftOr(fabricLoaderMap()[version], InvalidLoaderVersionError.Fabric(version))
                }

                Type.QUILT -> {
                    val version = loaderVersion.substring("quilt".length).trim()
                    leftOr(quiltLoaderMap()[version], InvalidLoaderVersionError.Quilt(version))
                }

                Type.FORGE -> {
                    val version = loaderVersion.substring("forge".length).trim()
                    leftOr(
                        forgeMap(minecraftVersion)[version], InvalidLoaderVersionError.Forge(version, minecraftVersion)
                    )
                }

                else -> Either.right(InvalidLoaderVersionError.UnknownLoader)
            }
        }

        suspend fun forVersion(
            loaderType: Type, loaderVersion: String, minecraftVersion: String?
        ): Either<LoaderVersion, InvalidLoaderVersionError> {
            return when (loaderType) {
                Type.FABRIC -> {
                    leftOr(fabricLoaderMap()[loaderVersion], InvalidLoaderVersionError.Fabric(loaderVersion))
                }

                Type.FORGE -> {
                    leftOr(
                        forgeMap(minecraftVersion)[loaderVersion],
                        InvalidLoaderVersionError.Forge(loaderVersion, minecraftVersion)
                    )
                }

                Type.QUILT -> {
                    leftOr(quiltLoaderMap()[loaderVersion], InvalidLoaderVersionError.Quilt(loaderVersion))
                }

                else -> right(InvalidLoaderVersionError.UnknownLoader)
            }
        }
    }

    abstract val type: Type

    abstract val version: String

    class Fabric internal constructor(override val version: String) : LoaderVersion() {
        override val type = Type.FABRIC

        override fun toString(): String {
            return "Fabric $version"
        }
    }

    class Forge internal constructor(override val version: String) : LoaderVersion() {
        override val type = Type.FORGE

        override fun toString(): String {
            return "Forge $version"
        }
    }

    class Quilt internal constructor(override val version: String) : LoaderVersion() {
        override val type = Type.QUILT

        override fun toString(): String {
            return "Quilt $version"
        }
    }

    enum class Type(val prettyName: String, val packwizName: String) {
        FABRIC("Fabric", "fabric"),
        LITELOADER("LiteLoader", "liteloader"),
        MODLOADER("Risugami's ModLoader", "modloader"),
        FORGE("Forge", "forge"),
        QUILT("Quilt", "quilt"),
        RIFT("Rift", "rift");

        companion object {
            fun fromPackwizName(name: String): Type? {
                return values().firstOrNull { it.packwizName == name }
            }

            fun forVersion(version: String): Type? {
                val lowercase = version.lowercase()
                return when {
                    lowercase.startsWith("fabric") -> FABRIC
                    lowercase.startsWith("liteloader") -> LITELOADER
                    lowercase.startsWith("modloader") -> MODLOADER
                    lowercase.startsWith("risugami's modloader") -> MODLOADER
                    lowercase.startsWith("forge") -> FORGE
                    lowercase.startsWith("quilt") -> QUILT
                    lowercase.startsWith("rift") -> RIFT
                    else -> null
                }
            }
        }

        override fun toString(): String {
            return prettyName
        }
    }
}

sealed class InvalidLoaderVersionError {
    data class Fabric(val loaderVersion: String) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "'$loaderVersion' is not a real Fabric version."
        }
    }

    data class Forge(val loaderVersion: String, val minecraftVersion: String?) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return if (minecraftVersion == null) {
                "'$loaderVersion' is not a real Forge version."
            } else {
                "'$loaderVersion' is not a real Forge version for Minecraft '$minecraftVersion'."
            }
        }
    }

    data class Quilt(val loaderVersion: String) : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "'$loaderVersion' is not a real Quilt version."
        }
    }

    object UnknownLoader : InvalidLoaderVersionError() {
        override fun toString(): String {
            return "Loader strings must start with either 'Fabric', 'Forge', or 'Quilt'."
        }
    }
}
