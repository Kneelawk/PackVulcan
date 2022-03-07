import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "com.kneelawk"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)

    // Decompose
    implementation("com.arkivanov.decompose:decompose:0.5.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:0.5.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

    // TOML4J
    implementation("com.moandjiezana.toml:toml4j:0.7.2")

    // Use logback logger because nothing seems to have updated their log4j yet
    runtimeOnly("ch.qos.logback:logback-classic:1.3.0-alpha14")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "com.kneelawk.mrmpb.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ModrinthModpackBuilder"
            packageVersion = "1.0.0"
        }
    }
}
