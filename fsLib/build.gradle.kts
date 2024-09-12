import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = "4.1.0"

val mainClassName = "io.ktor.server.netty.EngineMain"

kotlin {
    jvmToolchain(17)
    jvm {
        compilations.all {
            java {
                targetCompatibility = JavaVersion.VERSION_17
            }
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set(mainClassName)
        }
    }
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kvision.server.ktor.koin)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
            }
        }

        val jvmMain by getting {
            dependencies {
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
                api(libs.ktor.server.call.logging)
                api(libs.ktor.server.compression)
                api(libs.ktor.server.core)
                api(libs.ktor.server.default.headers)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.sessions)
                api(libs.ktor.network.tls.certificates)
                api(libs.ktor.server.auto.head.response)
                api(libs.ktor.server.http.redirect)
                api(libs.ktor.server.partial.content)
                api(libs.ktor.server.content.negotiation)
                implementation(libs.logback.classic)
                api(libs.kmongo.coroutine.serialization)
                api(libs.kmongo.id.serialization)
                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.jdbc)
                api(libs.exposed.java.time)
                api(libs.kotlinx.datetime.jvm)
                api(libs.jtds)
                api(libs.mssql.jdbc)
            }
        }

        val jsMain by getting {
            dependencies {
                api(libs.kmongo.id)
                api(libs.kvision)
                api(libs.kvision.bootstrap)
                api(libs.kvision.bootstrap.icons)
                api(libs.kvision.bootstrap.upload)
                api(libs.kvision.datetime)
                api(libs.kvision.chart)
                api(libs.kvision.fontawesome)
                api(libs.kvision.imask)
                api(libs.kvision.jquery)
                api(libs.kvision.pace)
                api(libs.kvision.print)
                api(libs.kvision.react)
                api(libs.kvision.redux.kotlin)
                api(libs.kvision.rest)
                api(libs.kvision.richtext)
                api(libs.kvision.routing.navigo.ng)
                api(libs.kvision.tabulator)
                api(libs.kvision.tabulator.remote)
                api(libs.kvision.toastify)
                api(libs.kvision.tom.select)
                api(libs.kvision.tom.select.remote)
            }
        }
    }
}
