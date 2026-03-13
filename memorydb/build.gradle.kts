plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
    id("fslib-publishing")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "io.github.tfonrouge.fslib"
version = libs.versions.fsLib.get()

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
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

    // Ktor (for ApplicationCall)
    implementation(libs.ktor.server.core)

    implementation(libs.kotlinx.serialization.json)

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
