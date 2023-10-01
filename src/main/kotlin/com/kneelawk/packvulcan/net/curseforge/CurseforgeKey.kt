package com.kneelawk.packvulcan.net.curseforge

import mu.KotlinLogging
import java.nio.file.Files
import kotlin.io.path.Path

object CurseforgeKey {
    private val log = KotlinLogging.logger { }

    val CURSEFORGE_KEY = getCfKey()?.trim()

    private fun getCfKey(): String? {
        val envCfKey = System.getenv("CURSEFORGE_KEY")
        if (!envCfKey.isNullOrBlank()) {
            log.info("Loading Curseforge key from CURSEFORGE_KEY environment variable...")
            return envCfKey
        }

        val cfKeyFile = Path(System.getProperty("user.home"), ".CURSEFORGE_KEY")
        if (Files.exists(cfKeyFile)) {
            val cfFileKey = Files.readString(cfKeyFile)
            if (!cfFileKey.isNullOrBlank()) {
                log.info("Loading Curseforge key from ~/.CURSEFORGE_KEY file...")
                return cfFileKey
            }
        }

        // TODO: add logic for baked curseforge keys in releases

        log.info("No Curseforge key found. Curseforge integration disabled.")
        return null
    }

    val CURSEFORGE_ENABLED = !CURSEFORGE_KEY.isNullOrBlank()
}
