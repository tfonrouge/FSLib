import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    id("com.android.library") version "8.2.1"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = "3.0.2"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
}

val kvisionVersion: String by System.getProperties()
val serializationVersion: String by project
val exposedVersion: String by project
val ktor_version: String by project
val kmongoVersion: String by project
val kotlinxDatetimeVersion: String by project
val commonsCodecVersion: String by project
val logbackVersion: String by project

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
        publishLibraryVariants("release")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.kvision:kvision-server-ktor-koin:$kvisionVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                api("io.ktor:ktor-client-core:${ktor_version}")
                api("io.ktor:ktor-client-cio:${ktor_version}")
                api("io.ktor:ktor-client-auth:${ktor_version}")
                api("io.ktor:ktor-client-content-negotiation:${ktor_version}")
                api("io.ktor:ktor-client-encoding:$ktor_version")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                api("io.ktor:ktor-client-serialization:${ktor_version}")
                api("io.ktor:ktor-server-auth:$ktor_version")
                api("io.ktor:ktor-server-auth-jwt:$ktor_version")
                api("io.ktor:ktor-server-call-logging:$ktor_version")
                api("io.ktor:ktor-server-compression:$ktor_version")
                api("io.ktor:ktor-server-core:$ktor_version")
                api("io.ktor:ktor-server-default-headers:$ktor_version")
                api("io.ktor:ktor-server-netty:$ktor_version")
                api("io.ktor:ktor-server-sessions:$ktor_version")
                api("io.ktor:ktor-network-tls-certificates:$ktor_version")
                api("io.ktor:ktor-server-auto-head-response:$ktor_version")
                api("io.ktor:ktor-server-http-redirect:$ktor_version")
                api("io.ktor:ktor-server-partial-content:$ktor_version")
                api("io.ktor:ktor-server-content-negotiation:$ktor_version")
                api("ch.qos.logback:logback-classic:$logbackVersion")
                api("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")
                api("org.litote.kmongo:kmongo-id-serialization:$kmongoVersion")
                api("org.jetbrains.exposed:exposed-core:$exposedVersion")
                api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                api("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$kotlinxDatetimeVersion")
                api("net.sourceforge.jtds:jtds:1.3.1")
                api("com.microsoft.sqlserver:mssql-jdbc:9.4.0.jre8")
//                api("org.mongodb:mongodb-driver-kotlin-coroutine:4.11.0")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.litote.kmongo:kmongo-id:$kmongoVersion")
                api("io.kvision:kvision:$kvisionVersion")
                api("io.kvision:kvision-bootstrap:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                api("io.kvision:kvision-datetime:$kvisionVersion")
                api("io.kvision:kvision-chart:$kvisionVersion")
                api("io.kvision:kvision-fontawesome:$kvisionVersion")
                api("io.kvision:kvision-imask:$kvisionVersion")
                api("io.kvision:kvision-jquery:$kvisionVersion")
                api("io.kvision:kvision-pace:$kvisionVersion")
                api("io.kvision:kvision-print:$kvisionVersion")
                api("io.kvision:kvision-react:$kvisionVersion")
                api("io.kvision:kvision-redux-kotlin:$kvisionVersion")
                api("io.kvision:kvision-rest:$kvisionVersion")
                api("io.kvision:kvision-richtext:$kvisionVersion")
                api("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                api("io.kvision:kvision-tabulator:$kvisionVersion")
                api("io.kvision:kvision-tabulator-remote:$kvisionVersion")
                api("io.kvision:kvision-toastify:$kvisionVersion")
                api("io.kvision:kvision-tom-select:$kvisionVersion")
                api("io.kvision:kvision-tom-select-remote:$kvisionVersion")
            }
        }

        val androidMain by getting {

        }
    }
}

android {
    namespace = "com.fonrouge.fsLib"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependencies {
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
        implementation("androidx.activity:activity-compose:1.8.2")
        implementation(platform("androidx.compose:compose-bom:2024.01.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3:1.2.0-rc01")
        implementation("androidx.compose.material:material-icons-extended")
        implementation("androidx.navigation:navigation-compose:2.7.6")

        implementation("androidx.paging:paging-compose:3.2.1")
        /* scanner service provided by Google Play */
        implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
        api("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

        implementation("androidx.camera:camera-camera2:1.4.0-alpha04")
        implementation("androidx.camera:camera-lifecycle:1.4.0-alpha04")
        implementation("androidx.camera:camera-view:1.4.0-alpha04")

        api("com.google.mlkit:barcode-scanning:17.2.0")

        /* permission*/
        implementation("com.google.accompanist:accompanist-permissions:0.32.0")
        /* replacement for pullRefresh that doesn't exist in Material3 */
        api("eu.bambooapps:compose-material3-pullrefresh:1.0.1")
        /* multi-button floating action button */
        api("com.github.iamageo:MultiFab:1.0.6")

        implementation("io.ktor:ktor-client-cio:2.3.7")
        implementation("io.ktor:ktor-client-okhttp:2.3.7")
        implementation("io.ktor:ktor-client-android:2.3.7")
        implementation("io.ktor:ktor-client-auth:2.3.7")
        implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
        implementation("io.ktor:ktor-client-serialization:2.3.7")
        implementation("io.ktor:ktor-client-logging:2.3.7")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
        debugImplementation("androidx.compose.ui:ui-tooling")
        debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.0-alpha01")
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
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
