package com.kneelawk.packvulcan.engine.packwiz

import com.kneelawk.packvulcan.engine.hash.HashHelper
import com.kneelawk.packvulcan.engine.modinfo.ModFileInfo
import com.kneelawk.packvulcan.model.HashFormat
import com.kneelawk.packvulcan.model.NewModpack
import com.kneelawk.packvulcan.model.packwiz.TomlHelper
import com.kneelawk.packvulcan.model.packwiz.index.FileToml
import com.kneelawk.packvulcan.model.packwiz.index.IndexToml
import com.kneelawk.packvulcan.model.packwiz.mod.ModToml
import com.kneelawk.packvulcan.model.packwiz.pack.IndexObjectToml
import com.kneelawk.packvulcan.model.packwiz.pack.PackToml
import com.kneelawk.packvulcan.model.packwiz.pack.VersionsToml
import kotlinx.coroutines.*
import mu.KotlinLogging
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.eclipse.jgit.ignore.FastIgnoreRule
import org.eclipse.jgit.ignore.IgnoreNode
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.*

/**
 * This class is designed to be loaded, read/edited, and then written back to the filesystem, much like packwiz itself
 * does when running a command. This structure itself should not be a permanent means of data storage.
 */
class PackwizProject(
    val projectDir: Path, var pack: PackToml, var index: IndexToml,
    val files: MutableList<PackwizFile>, val packwizIgnore: IgnoreNode
) {
    companion object {
        private const val INDEX_FILENAME = "index.toml"
        private const val PACK_FILENAME = "pack.toml"
        private const val PACKWIZIGNORE_FILENAME = ".packwizignore"
        private const val MODS_DIRNAME = "mods"
        private const val METAFILE_EXTENSION = ".toml"

        private val log = KotlinLogging.logger { }

        // Copied from https://github.com/packwiz/packwiz/blob/3ab85821e9669f017b07fb6cffee80ee60a5150d/core/index.go#L172
        private val packwizIgnoreRules = listOf(
            // Defaults (can be overridden with a negating pattern preceded with !)

            // Exclude Git metadata
            FastIgnoreRule(".git/**"),
            FastIgnoreRule(".gitattributes"),
            FastIgnoreRule(".gitignore"),

            // Exclude exported CurseForge zip files
            FastIgnoreRule("/*.zip"),

            // Exclude exported Modrinth packs
            FastIgnoreRule("*.mrpack"),

            // Exclude packwiz binaries, if the user puts them in their pack folder
            FastIgnoreRule("packwiz.exe"),
            FastIgnoreRule("packwiz"),
            // Note: also excludes packwiz/ as a directory - you can negate this pattern if you want a directory called packwiz
        )

        fun isPackFile(packFile: Path): Boolean {
            return packFile.name == PACK_FILENAME
        }

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

            return PackwizProject(
                newModpack.location, pack, index, mutableListOf(), IgnoreNode(packwizIgnoreRules.toMutableList())
            )
        }

        suspend fun loadFromProjectDir(projectDir: Path): PackwizProject {
            return loadExisting(projectDir.resolve(PACK_FILENAME), projectDir)
        }

        private suspend fun loadExisting(packFile: Path, projectDir: Path): PackwizProject = coroutineScope {
            val indexFile = projectDir.resolve(INDEX_FILENAME)
            val packwizIgnoreFile = projectDir.resolve(PACKWIZIGNORE_FILENAME)

            val packwizIgnoreDeferred = async(Dispatchers.IO) {
                val ignore = IgnoreNode(packwizIgnoreRules.toMutableList())
                if (packwizIgnoreFile.exists()) {
                    packwizIgnoreFile.inputStream().use {
                        ignore.parse(".packwizignore", it)
                    }
                }
                ignore
            }

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
                async(Dispatchers.IO) {
                    val path = projectDir.resolve(indexElement.file)
                    val hashFormat = indexElement.hashFormat ?: index.hashFormat

                    if (path.exists()) {
                        val (packwizFile, fileHash) = if (indexElement.metafile) {
                            val fileSource = FileSystem.SYSTEM.source(path.toOkioPath())
                            val hashSource = hashFormat.makeSource(fileSource)
                            val toml = hashSource.use { TomlHelper.read(ModToml, it) }
                            PackwizMetaFile(
                                indexElement.file, indexElement.alias, indexElement.preserve, toml
                            ) to hashSource.hashString()
                        } else {
                            val hash = HashHelper.hash(path, hashFormat)
                            packwizRealFile(
                                indexElement.file, indexElement.alias, indexElement.preserve, path
                            ) to hash
                        }

                        if (fileHash != indexElement.hash) {
                            log.warn(
                                "File '${indexElement.file}' has differing hash from known hash. This file was likely edited outside of packwiz."
                            )
                        }

                        packwizFile
                    } else {
                        // don't bother trying to load files that don't exist anymore
                        null
                    }
                }
            }

            val files = loadingFiles.awaitAll().asSequence().filterNotNull().sortedBy { it.filePath }.toMutableList()

            val packwizIgnore = packwizIgnoreDeferred.await()

            PackwizProject(projectDir, pack, index, files, packwizIgnore)
        }

        private suspend fun packwizRealFile(
            filePath: String, alias: String?, preserve: Boolean, path: Path
        ): PackwizRealFile = withContext(Dispatchers.IO) {
            if (path.name.endsWith(".jar")) {
                val info = ModFileInfo.getFileInfo(path)
                if (info != null) {
                    PackwizModFile(filePath, alias, preserve, path, info)
                } else {
                    PackwizResourceFile(filePath, alias, preserve, path)
                }
            } else {
                PackwizResourceFile(filePath, alias, preserve, path)
            }
        }
    }

    private val modsDirname = pack.options?.modsFolder ?: MODS_DIRNAME

    val modsDir: Path
        get() = projectDir.resolve(modsDirname)

    fun getMods(): List<PackwizMod> {
        return files.mapNotNull { packwizFile ->
            if (packwizFile is PackwizMod && packwizFile.filePath.startsWith(modsDirname)) {
                packwizFile
            } else {
                null
            }
        }
    }

    fun setMods(mods: List<PackwizMod>) {
        files.removeAll {
            it is PackwizMod && it.filePath.startsWith(modsDirname)
        }
        files.addAll(mods.distinctBy { it.filePath })
        files.sortBy { it.filePath }
    }

    private suspend fun updateIndex() = coroutineScope {
        val loadingFiles = files.map { file ->
            async(Dispatchers.IO) {
                when (file) {
                    is PackwizMetaFile -> {
                        FileToml(
                            file.filePath, TomlHelper.hash(file.toml, index.hashFormat), file.alias, null, true,
                            file.preserve
                        )
                    }
                    is PackwizRealFile -> {
                        FileToml(
                            file.filePath, HashHelper.hash(file.file, index.hashFormat), file.alias, null, false,
                            file.preserve
                        )
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

    private suspend fun getCurrentFileSystemFiles() = withContext(Dispatchers.IO) {
        Files.walk(projectDir).use { stream ->
            stream.map { it to it.relativeTo(projectDir).invariantSeparatorsPathString }
                .filter { (path, relative) ->
                    !path.isDirectory()
                            && relative != PACK_FILENAME
                            && relative != INDEX_FILENAME
                            && relative != PACKWIZIGNORE_FILENAME
                            && packwizIgnore.isIgnored(
                        relative, false
                    ) != IgnoreNode.MatchResult.IGNORED
                }
                .collect(Collectors.toList())
        }
    }

    suspend fun refresh() = coroutineScope {
        val currentMetaFiles = files.asSequence().mapNotNull {
            if (it is PackwizMetaFile) {
                it.filePath
            } else {
                null
            }
        }.toSet()

        val toRefresh: List<Pair<Path, String>> = getCurrentFileSystemFiles()

        // first, we collect a set of all the packwiz files that exist
        val existing = toRefresh.asSequence().map { it.second }.toSet()

        // then, we remove the packwiz files that no longer exist
        files.retainAll { existing.contains(it.filePath) }
        // Note: we not need to handle file removal here because the refresh() operation is where new files are brought
        // into the project

        // next, we collect a set of all the packwiz files that we're aware of
        val awareOf = files.asSequence().map { it.filePath }.toSet()

        // then, we add the packwiz files that now exist that we were not aware of
        val newFilesDeferred = toRefresh.mapNotNull { (path, relative) ->
            if (awareOf.contains(relative)) {
                null
            } else {
                async(Dispatchers.IO) {
                    if ((relative.startsWith(modsDirname) && relative.endsWith(
                            METAFILE_EXTENSION
                        )) || currentMetaFiles.contains(relative)
                    ) {
                        try {
                            val toml = FileSystem.SYSTEM.source(path.toOkioPath()).use {
                                TomlHelper.read(ModToml, it)
                            }
                            PackwizMetaFile(relative, null, false, toml)
                        } catch (e: IOException) {
                            log.warn(
                                "Encountered exception when parsing what appeared to be a packwiz meta-file but actually wasn't.",
                                e
                            )
                            packwizRealFile(relative, null, false, path)
                        } catch (e: IllegalStateException) {
                            log.warn(
                                "Encountered exception when parsing what appeared to be a packwiz meta-file but actually wasn't.",
                                e
                            )
                            packwizRealFile(relative, null, false, path)
                        }
                    } else {
                        packwizRealFile(relative, null, false, path)
                    }
                }
            }
        }

        // await the files and add them
        val newFiles = newFilesDeferred.awaitAll()
        files.addAll(newFiles)

        // finally, we update the index and the hash of the index in the pack file
        updateHashes()
    }

    suspend fun write(): Unit = coroutineScope {
        updateHashes()

        val indexFile = projectDir.resolve(INDEX_FILENAME).toOkioPath()
        val packFile = projectDir.resolve(PACK_FILENAME).toOkioPath()

        // we need to remove the actual files that we removed from the project
        val awareOf = files.asSequence().map { it.filePath }.toSet()
        val actualFiles = getCurrentFileSystemFiles()
        for ((path, relative) in actualFiles) {
            if (!awareOf.contains(relative)) {
                try {
                    path.deleteExisting()
                } catch (e: IOException) {
                    log.error("Encountered exception while deleting removed project file.", e)
                }
            }
        }

        // now we can write out all the files we're aware of
        files.map { packwizFile ->
            launch(Dispatchers.IO) {
                val path = projectDir.resolve(packwizFile.filePath)

                when (packwizFile) {
                    is PackwizMetaFile -> {
                        FileSystem.SYSTEM.sink(path.toOkioPath()).use { TomlHelper.write(packwizFile.toml, it) }
                    }
                    is PackwizRealFile -> {
                        if (path.normalize() != packwizFile.file.normalize()) {
                            log.warn(
                                "File '${packwizFile.filePath}' should be at '$path' but is at '${packwizFile.file}' instead. Copying to the correct location..."
                            )
                            // I am not sure this is really necessary, as there should be no circumstance where these two paths are different.
                            Files.copy(packwizFile.file, path)
                        }
                    }
                }
            }
        }

        launch(Dispatchers.IO) {
            FileSystem.SYSTEM.sink(indexFile).use { TomlHelper.write(index, it) }
        }

        launch(Dispatchers.IO) {
            FileSystem.SYSTEM.sink(packFile).use { TomlHelper.write(pack, it) }
        }

        // this `coroutineScope` only completes once all the `launch` statements complete
    }
}