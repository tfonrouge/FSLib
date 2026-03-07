plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kilua.rpc)
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
            implementation(project(":base"))
            implementation(libs.kilua.rpc.ktor)
            implementation(libs.kvision.common.remote)
            implementation(libs.kotlinx.datetime)
        }

        jvmMain.dependencies {
            implementation(kotlin("reflect"))
            api(libs.ktor.client.core)
            api(libs.ktor.client.cio)
            api(libs.ktor.client.auth)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.encoding)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.ktor.client.serialization)
            api(libs.ktor.server.auth)
            api(libs.ktor.server.auth.jwt)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.ktor.server.compression)
            api(libs.ktor.server.core)
            implementation(libs.ktor.server.default.headers)
            implementation(libs.ktor.server.forwarded.header)
            implementation(libs.ktor.server.netty)
            api(libs.ktor.server.sessions)
            implementation(libs.ktor.network.tls.certificates)
            implementation(libs.ktor.server.auto.head.response)
            implementation(libs.ktor.server.http.redirect)
            implementation(libs.ktor.server.partial.content)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.status.pages)
            api(libs.kmongo.coroutine.serialization)
            api(libs.kmongo.id.serialization)
            api(libs.exposed.core)
            api(libs.exposed.dao)
            api(libs.exposed.jdbc)
            api(libs.exposed.java.time)
            api(libs.kotlinx.datetime.jvm)
            implementation(libs.jtds)
            implementation(libs.mssql.jdbc)
        }

        jsMain.dependencies {
        }
    }
}

tasks.named("sourcesJar") {
    dependsOn("kspCommonMainKotlinMetadata")
}

