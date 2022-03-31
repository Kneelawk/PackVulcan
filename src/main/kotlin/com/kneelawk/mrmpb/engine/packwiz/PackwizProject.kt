package com.kneelawk.mrmpb.engine.packwiz

import com.kneelawk.mrmpb.engine.hash.HashHelper
import com.kneelawk.mrmpb.model.NewModpack
import com.kneelawk.mrmpb.model.packwiz.HashFormat
import com.kneelawk.mrmpb.model.packwiz.TomlHelper
import com.kneelawk.mrmpb.model.packwiz.index.FileToml
import com.kneelawk.mrmpb.model.packwiz.index.IndexToml
import com.kneelawk.mrmpb.model.packwiz.mod.ModToml
import com.kneelawk.mrmpb.model.packwiz.pack.IndexObjectToml
import com.kneelawk.mrmpb.model.packwiz.pack.PackToml
import com.kneelawk.mrmpb.model.packwiz.pack.VersionsToml
import kotlinx.coroutines.*
import mu.KotlinLogging
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import java.nio.file.Path

/**
 * This class is designed to be loaded, read/edited, and then written back to the filesystem, much like packwiz itself
 * does when running a command. This structure itself should not be a permanent means of data storage.
 */
class PackwizProject(
    val projectDir: Path, var pack: PackToml, var index: IndexToml,
    val files: MutableMap<String, PackwizFile> = mutableMapOf()
) {
    companion object {
        private const val INDEX_FILENAME = "index.toml"
        private const val PACK_FILENAME = "pack.toml"

        private val log = KotlinLogging.logger { }

        suspend fun createNew(newModpack: NewModpack): PackwizProject {
            val index = IndexToml(HashFormat.SHA256, listOf())

            val indexObject =
                IndexObjectToml(INDEX_FILENAME, HashFormat.SHA256, TomlHelper.hash(index, HashFormat.SHA256))

            val loaderVersion = newModpack.loaderVersion
            val loaderVersions = mapOf(loaderVersion.type.packwizName to loaderVersion.version)
            val versions = VersionsToml(newModpack.minecraftVersion.version, loaderVersions)

            val pack = PackToml(
                newModpack.name, newModpack.author, newModpack.version, null, PackToml.DEFAULT_PACK_FORMAT, indexObject,
                versions, null
            )

            return PackwizProject(newModpack.location, pack, index)
        }

        suspend fun loadExisting(packFile: Path): PackwizProject {
            val projectDir = packFile.parent
            val indexFile = projectDir.resolve(INDEX_FILENAME)

            val pack = withContext(Dispatchers.IO) {
                FileSystem.SYSTEM.source(packFile.toOkioPath()).use {
                    TomlHelper.read(PackToml, it)
                }
            }

            val (index, indexHash) = withContext(Dispatchers.IO) {
                val fileSource = FileSystem.SYSTEM.source(indexFile.toOkioPath())
                val hashSource = pack.index.hashFormat.makeSource(fileSource)
                val index = hashSource.use { TomlHelper.read(IndexToml, it) }
                index to hashSource.hashString()
            }

            if (indexHash != pack.index.hash) {
                log.warn(
                    "index.toml file hash differs from known hash. This file was likely edited outside of packwiz."
                )
            }

            val loadingFiles = index.files.map { indexElement ->
                coroutineScope {
                    async(Dispatchers.IO) {
                        val path = projectDir.resolve(indexElement.file)
                        val hashFormat = indexElement.hashFormat ?: index.hashFormat

                        val (packwizFile, fileHash) = if (indexElement.metafile) {
                            val fileSource = FileSystem.SYSTEM.source(path.toOkioPath())
                            val hashSource = hashFormat.makeSource(fileSource)
                            val toml = hashSource.use { TomlHelper.read(ModToml, it) }
                            PackwizFile.MetaFile(
                                indexElement.alias, indexElement.preserve, toml
                            ) to hashSource.hashString()
                        } else {
                            val hash = HashHelper.hash(path, hashFormat)
                            PackwizFile.RealFile(indexElement.alias, indexElement.preserve, path) to hash
                        }

                        if (fileHash != indexElement.hash) {
                            log.warn(
                                "File '${indexElement.file}' has differing hash from known hash. This file was likely edited outside of packwiz."
                            )
                        }

                        indexElement.file to packwizFile
                    }
                }
            }

            val files = loadingFiles.awaitAll().toMap().toMutableMap()

            return PackwizProject(projectDir, pack, index, files)
        }
    }

    private suspend fun updateIndex() {
        val loadingFiles = this.files.map { (filename, file) ->
            coroutineScope {
                async(Dispatchers.IO) {
                    when (file) {
                        is PackwizFile.MetaFile -> {
                            FileToml(
                                filename, TomlHelper.hash(file.toml, index.hashFormat), file.alias, null, true,
                                file.preserve
                            )
                        }
                        is PackwizFile.RealFile -> {
                            FileToml(
                                filename, HashHelper.hash(file.file, index.hashFormat), file.alias, null, false,
                                file.preserve
                            )
                        }
                    }
                }
            }
        }

        val files = loadingFiles.awaitAll()

        index = index.copy(files = files)
    }

    private suspend fun updateIndexHash() {
        pack = pack.copy(index = pack.index.copy(hash = TomlHelper.hash(index, pack.index.hashFormat)))
    }

    suspend fun updateHashes() {
        updateIndex()
        updateIndexHash()
    }

    suspend fun write() {
        updateHashes()

        val indexFile = projectDir.resolve(INDEX_FILENAME).toOkioPath()
        val packFile = projectDir.resolve(PACK_FILENAME).toOkioPath()

        val jobs = index.files.map { indexElement ->
            coroutineScope {
                launch(Dispatchers.IO) {
                    val path = projectDir.resolve(indexElement.file)

                    when (val packwizFile = files[indexElement.file]) {
                        is PackwizFile.MetaFile -> {
                            FileSystem.SYSTEM.sink(path.toOkioPath()).use { TomlHelper.write(packwizFile.toml, it) }
                        }
                        is PackwizFile.RealFile -> {
                            if (path.normalize() != packwizFile.file.normalize()) {
                                log.warn(
                                    "File '${indexElement.file}' should be at '$path' but is at '${packwizFile.file}' instead. Copying to the correct location..."
                                )
                                // I am not sure this is really necessary, as there should be no circumstance where these two paths are different.
                                Files.copy(packwizFile.file, path)
                            }
                        }
                        null -> log.warn("Tried to save unknown file '${indexElement.file}'")
                    }
                }
            }
        }

        jobs.joinAll()

        withContext(Dispatchers.IO) {
            FileSystem.SYSTEM.sink(indexFile).use { TomlHelper.write(index, it) }
        }

        withContext(Dispatchers.IO) {
            FileSystem.SYSTEM.sink(packFile).use { TomlHelper.write(pack, it) }
        }
    }
}