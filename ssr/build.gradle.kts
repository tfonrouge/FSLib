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
    api(project(":fullstack")) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }
    api(project(":core")) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }
    implementation(project(":mongodb"))
    implementation(kotlin("reflect"))
    implementation(libs.kotlinx.datetime.jvm)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.sessions)
    api(libs.ktor.server.html.builder)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-server-content-negotiation:${libs.versions.ktor.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}
