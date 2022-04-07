pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version (kotlinVersion)
        kotlin("plugin.serialization") version (kotlinVersion)
        val composeVersion: String by settings
        id("org.jetbrains.compose") version (composeVersion)
    }
}
rootProject.name = "PackVulcan"

