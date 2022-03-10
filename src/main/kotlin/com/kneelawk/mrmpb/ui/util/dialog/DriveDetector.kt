package com.kneelawk.mrmpb.ui.util.dialog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.fileStore
import kotlin.io.path.isReadable
import kotlin.io.path.pathString

object DriveDetector {
    val dfOutputPattern =
        Regex("""(?<des>\S+)\s+(?<size>\S+)\s+(?<used>\S+)\s+(?<avail>\S+)\s+(?<usepc>\S+)\s+(?<mount>.+)""")

    private suspend fun unixDetectDrives(): List<DriveItem> {
        val output = withContext(Dispatchers.IO) {
            val proc = ProcessBuilder("df", "-hP")
                .directory(File(System.getProperty("user.home")))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            // A time limit of 3 seconds because our refresh-rate is 5 seconds, to prevent the drive-detection from
            // feeling sluggish
            proc.waitFor(3, TimeUnit.SECONDS)

            proc.inputReader().readText()
        }.lines()

        return output.asFlow()
            .drop(1) // skip the header line
            .mapNotNull { line ->
                val groups = dfOutputPattern.matchEntire(line)?.groups ?: return@mapNotNull null
                val des = groups["des"]?.value ?: return@mapNotNull null
                val mount = groups["mount"]?.value ?: return@mapNotNull null
                des to mount
            }
            .filter { it.first.startsWith("/dev") }
            .map { (_, mount) ->
                val path = Paths.get(mount)
                val fileStore = withContext(Dispatchers.IO) { path.fileStore() }
                val name = fileStore.name()

                DriveItem(path, "$mount ($name)")
            }
            .filter { withContext(Dispatchers.IO) { it.path.isReadable() } }
            .toList()
    }

    private suspend fun windowsDetectDrives(): List<DriveItem> {
        val paths = withContext(Dispatchers.IO) {
            FileSystems.getDefault().rootDirectories.toList()
        }

        return paths.asFlow()
            .mapNotNull { path ->
                val fileStore = try {
                    withContext(Dispatchers.IO) { path.fileStore() }
                } catch (e: java.nio.file.FileSystemException) {
                    return@mapNotNull null
                }
                val name = fileStore.name()

                DriveItem(path, "${path.pathString} ($name)")
            }
            .filter { withContext(Dispatchers.IO) { it.path.isReadable() } }
            .toList()
    }

    suspend fun detectDrives(): List<DriveItem> {
        val os = System.getProperty("os.name")
        return if (os.startsWith("Windows")) {
            windowsDetectDrives()
        } else {
            unixDetectDrives()
        }
    }
}