import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
//    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

val libVersion = "1.0.0"

group = "com.fonrouge.kmpLib"
version = libVersion

repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
}

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
    androidTarget {
        publishLibraryVariants("debug", "release")
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
                api(libs.logback.classic)
                api(libs.kmongo.coroutine.serialization)
                api(libs.kmongo.id.serialization)
                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.jdbc)
                api(libs.exposed.java.time)
                api(libs.kotlinx.datetime.jvm)
                api(libs.jtds)
                api(libs.mssql.jdbc)
//                api("org.mongodb:mongodb-driver-kotlin-coroutine:4.11.0")
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

        val androidMain by getting {

        }
    }
}

android {
    namespace = "com.fonrouge.kmpLib"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependencies {
        implementation(libs.core.ktx)
        implementation(libs.lifecycle.runtime.ktx)
        implementation(libs.activity.compose)
        implementation(libs.compose.bom)
        implementation(libs.ui)
        implementation(libs.ui.graphics)
        implementation(libs.ui.tooling.preview)
        implementation(libs.material3)
        implementation(libs.material.icons.extended)
        implementation(libs.navigation.compose)

        implementation(libs.paging.compose)
        /* scanner service provided by Google Play */
        implementation(libs.play.services.code.scanner)
        implementation(libs.lifecycle.viewmodel.compose)
        api(libs.lifecycle.runtime.compose)

        implementation(libs.camera.camera2)
        implementation(libs.camera.lifecycle)
        implementation(libs.camera.view)

        api(libs.barcode.scanning)

        /* permission*/
        implementation(libs.accompanist.permissions)
        /* replacement for pullRefresh that doesn't exist in Material3 */
        api(libs.compose.material3.pullrefresh)
        /* multi-button floating action button */
        api(libs.multifab)

        implementation(libs.ktor.client.cio)
        implementation(libs.ktor.client.okhttp)
        implementation(libs.ktor.client.android)
        implementation(libs.ktor.client.auth)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.serialization)
        implementation(libs.ktor.client.logging)

        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        androidTestImplementation(libs.compose.bom)
        androidTestImplementation(libs.ui.test.junit4)
        debugImplementation(libs.ui.tooling)
        debugImplementation(libs.ui.test.manifest)
    }
    /*
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
            getByName("debug") {
                isMinifyEnabled = false
            }
        }
    */
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}
