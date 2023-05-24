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

    // Desktop version of Accompanist
    val accompanistVersion: String by project
    implementation("ca.gosyer:accompanist-flowlayout:$accompanistVersion")

    // Coroutines
    val coroutinesVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")

    // Serialization
    val serializationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Ktor Client
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

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

    // JGit
    val jgitVersion: String by project
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")

    // TOML4J
    val toml4jVersion: String by project
    implementation("com.moandjiezana.toml:toml4j:$toml4jVersion")

    // SemVer
    val javaSemverVersion: String by project
    implementation("com.github.zafarkhaja:java-semver:$javaSemverVersion")

    // load WEBP images in ImageIO
    val sejdaWebpVersion: String by project
    implementation("org.sejda.imageio:webp-imageio:$sejdaWebpVersion")

    // Flexmark
    val flexmarkVersion: String by project
    implementation("com.vladsch.flexmark:flexmark-all:$flexmarkVersion") {
        exclude(module = "flexmark-pdf-converter")
    }

    // Markdown can have raw HTML inside it :(
    // That means we need to parse HTML
    val jsoupVersion: String by project
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Force the use of a specific Gson version
    val gsonVersion: String by project
    implementation("com.google.code.gson:gson:$gsonVersion")

    // Reload4j Slf4j backend
    val slf4jVersion: String by project
    runtimeOnly("org.slf4j:slf4j-reload4j:$slf4jVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
//        // FIXME: Compose Desktop doesn't support Kotlin v1.7.10 yet so we're suppressing compatibility checks
//        freeCompilerArgs += listOf(
//            "-P",
//            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
//        )
    }
}

compose.desktop {
    application {
        mainClass = "com.kneelawk.packvulcan.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PackVulcan"
            packageVersion = "1.0.0"
        }
    }
}
