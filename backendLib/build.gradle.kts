plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "com.fonrouge.backendLib"
version = libs.versions.fsLib.get()

repositories {
    mavenCentral()
}

kotlin {
//    jvmToolchain(21)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xallow-kotlin-package",
            )
        }
    }
    js(IR) {
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
        }
        binaries.library()
        compilerOptions {
            target.set("es2015")
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":fsLib"))
            implementation(libs.kilua.rpc.ktor)
        }

        jvmMain.dependencies {
            implementation(kotlin("reflect"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.server.auth)
            implementation(libs.ktor.server.auth.jwt)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.ktor.server.compression)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.default.headers)
            implementation(libs.ktor.server.forwarded.header)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.sessions)
            implementation(libs.ktor.network.tls.certificates)
            implementation(libs.ktor.server.auto.head.response)
            implementation(libs.ktor.server.http.redirect)
            implementation(libs.ktor.server.partial.content)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.kmongo.coroutine.serialization)
            implementation(libs.kmongo.id.serialization)
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.java.time)
            implementation(libs.kotlinx.datetime.jvm)
            implementation(libs.jtds)
            implementation(libs.mssql.jdbc)
        }

        jsMain.dependencies {
        }
    }
}
