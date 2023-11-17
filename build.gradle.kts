import com.google.devtools.ksp.gradle.KspTaskMetadata
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    id("com.android.library") version "8.1.4"
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
    kotlin("multiplatform") version kotlinVersion
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = "1.9.5"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
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
        publishLibraryVariants("release", "debug")
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
                api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$kotlinxDatetimeVersion")
                api("net.sourceforge.jtds:jtds:1.3.1")
                api("com.microsoft.sqlserver:mssql-jdbc:9.4.0.jre8")
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
//            kotlin.srcDir("build/generated-src/frontend")
        }

        val androidMain by getting {
            dependencies {
                api("io.ktor:ktor-client-cio:${ktor_version}")
                api("io.ktor:ktor-client-auth:${ktor_version}")
                api("io.ktor:ktor-client-content-negotiation:${ktor_version}")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                api("io.ktor:ktor-client-serialization:${ktor_version}")
                api("io.ktor:ktor-client-logging:$ktor_version")
            }
        }
    }
}

android {
    namespace = "com.fonrouge.fsLib"
    compileSdk = 33
    defaultConfig {
        minSdk = 27
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/*
Required to avoid error on dependency not declared on gradle v8.0.2
TODO: find out how to solve
 */
tasks.withType<KspTaskMetadata> {
    dependsOn(tasks.getByPath(":compileReleaseKotlinAndroid"))
    dependsOn(tasks.getByPath(":compileDebugKotlinAndroid"))
    dependsOn(tasks.getByPath(":androidReleaseSourcesJar"))
    dependsOn(tasks.getByPath(":androidDebugSourcesJar"))
    dependsOn(tasks.getByPath(":jvmSourcesJar"))
    dependsOn(tasks.getByPath(":sourcesJar"))
}
