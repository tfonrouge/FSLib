plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "com.fonrouge.fsLib"
version = libs.versions.fsLib.get()

repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
//    jvmToolchain(21)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kilua.rpc.ktor)
            implementation(libs.kvision.common.remote)
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
            // TODO: change api for implementation. api allows to export ApplicationCall class needed on IApiItem
            api(libs.ktor.server.core)
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
            implementation(libs.kvision)
            implementation(libs.kvision.bootstrap)
            implementation(libs.kvision.bootstrap.icons)
            implementation(libs.kvision.bootstrap.upload)
            implementation(libs.kvision.datetime)
            implementation(libs.kvision.chart)
            implementation(libs.kvision.fontawesome)
            implementation(libs.kvision.imask)
            implementation(libs.kvision.i18n)
            implementation(libs.kvision.jquery)
            implementation(libs.kvision.pace)
            implementation(libs.kvision.print)
            implementation(libs.kvision.react)
            implementation(libs.kvision.redux.kotlin)
            implementation(libs.kvision.rest)
            implementation(libs.kvision.richtext)
            implementation(libs.kvision.routing.navigo.ng)
            implementation(libs.kvision.state)
            implementation(libs.kvision.tabulator)
            implementation(libs.kvision.tabulator.remote)
            implementation(libs.kvision.toastify)
            implementation(libs.kvision.tom.select)
            implementation(libs.kvision.tom.select.remote)
        }
    }
}
