import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    id("com.android.library") version "8.2.2"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = "3.2.7"

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
val ktorAndroidVersion: String by project
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
        publishLibraryVariants("debug", "release")
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
                api("io.ktor:ktor-client-core:${ktorAndroidVersion}")
                api("io.ktor:ktor-client-cio:${ktorAndroidVersion}")
                api("io.ktor:ktor-client-auth:${ktorAndroidVersion}")
                api("io.ktor:ktor-client-content-negotiation:${ktorAndroidVersion}")
                api("io.ktor:ktor-client-encoding:$ktorAndroidVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorAndroidVersion")
                api("io.ktor:ktor-client-serialization:${ktorAndroidVersion}")
                api("io.ktor:ktor-server-auth:$ktorAndroidVersion")
                api("io.ktor:ktor-server-auth-jwt:$ktorAndroidVersion")
                api("io.ktor:ktor-server-call-logging:$ktorAndroidVersion")
                api("io.ktor:ktor-server-compression:$ktorAndroidVersion")
                api("io.ktor:ktor-server-core:$ktorAndroidVersion")
                api("io.ktor:ktor-server-default-headers:$ktorAndroidVersion")
                api("io.ktor:ktor-server-netty:$ktorAndroidVersion")
                api("io.ktor:ktor-server-sessions:$ktorAndroidVersion")
                api("io.ktor:ktor-network-tls-certificates:$ktorAndroidVersion")
                api("io.ktor:ktor-server-auto-head-response:$ktorAndroidVersion")
                api("io.ktor:ktor-server-http-redirect:$ktorAndroidVersion")
                api("io.ktor:ktor-server-partial-content:$ktorAndroidVersion")
                api("io.ktor:ktor-server-content-negotiation:$ktorAndroidVersion")
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependencies {
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
        implementation("androidx.activity:activity-compose:1.9.1")
        implementation(platform("androidx.compose:compose-bom:2024.06.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3:1.2.1")
        implementation("androidx.compose.material:material-icons-extended")
        implementation("androidx.navigation:navigation-compose:2.7.7")

        implementation("androidx.paging:paging-compose:3.3.1")
        /* scanner service provided by Google Play */
        implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
        api("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

        implementation("androidx.camera:camera-camera2:1.4.0-beta02")
        implementation("androidx.camera:camera-lifecycle:1.4.0-beta02")
        implementation("androidx.camera:camera-view:1.4.0-beta02")

        api("com.google.mlkit:barcode-scanning:17.2.0")

        /* permission*/
        implementation("com.google.accompanist:accompanist-permissions:0.32.0")
        /* replacement for pullRefresh that doesn't exist in Material3 */
        api("eu.bambooapps:compose-material3-pullrefresh:1.0.1")
        /* multi-button floating action button */
        api("com.github.iamageo:MultiFab:1.0.6")

        implementation("io.ktor:ktor-client-cio:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-okhttp:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-android:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-auth:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-content-negotiation:$ktorAndroidVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-serialization:$ktorAndroidVersion")
        implementation("io.ktor:ktor-client-logging:$ktorAndroidVersion")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
        debugImplementation("androidx.compose.ui:ui-tooling")
        debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.0-beta06")
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
