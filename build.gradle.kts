import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)

    // Special Compose UI Elements
    val composeVersion: String by project
    implementation("org.jetbrains.compose.components:components-splitpane-desktop:$composeVersion")

    // Decompose
    val decomposeVersion: String by project
    implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:$decomposeVersion")

    // Coroutines
    val coroutinesVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    // Serialization
    val serializationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Ktor Client
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    val okioVersion: String by project
    implementation("com.squareup.okio:okio:$okioVersion")

    // Caffine
    val caffeineVersion: String by project
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    // Logging
    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    // Commons Codec
    val commonsCodecVersion: String by project
    implementation("commons-codec:commons-codec:$commonsCodecVersion")

    // TOML4J
    val toml4jVersion: String by project
    implementation("com.moandjiezana.toml:toml4j:$toml4jVersion")

    // SemVer
    val javaSemverVersion: String by project
    implementation("com.github.zafarkhaja:java-semver:$javaSemverVersion")

    // Use logback logger because nothing seems to have updated their log4j yet
    val logbackVersion: String by project
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
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
