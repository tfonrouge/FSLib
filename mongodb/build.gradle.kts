plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "com.fonrouge.fsLib"
version = libs.versions.fsLib.get()

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    api(project(":fullstack")) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }

    implementation(kotlin("reflect"))

    // MongoDB
    api(libs.kmongo.coroutine.serialization)
    api(libs.kmongo.id.serialization)

    // Ktor (for ApplicationCall, sessions)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.sessions)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime.jvm)

    // Kilua RPC (for RemoteFilter, RemoteSorter)
    implementation(libs.kilua.rpc.ktor) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }
}
