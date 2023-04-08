import com.google.devtools.ksp.gradle.KspTaskMetadata

plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    id("com.android.library")
    id("io.kvision") version kvisionVersion
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = "1.5.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

val kvisionVersion: String by System.getProperties()
val serializationVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project
val kmongoVersion: String by project
val kotlinxDatetimeVersion: String by project
val commonsCodecVersion: String by project
//val logbackVersion: String by project

kotlin {
    jvm("backend") {
        compilations.all {
            java {
                targetCompatibility = JavaVersion.VERSION_17
            }
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
    js("frontend") {
        browser {
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
            }
        }
        binaries.library()
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
//                api("ch.qos.logback:logback-classic:$logbackVersion")
//                api("io.kvision:kvision-server-ktor:$kvisionVersion")
                api("io.kvision:kvision-server-ktor-koin:$kvisionVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
            kotlin.srcDir("build/generated-src/common")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val backendMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
                api("commons-codec:commons-codec:$commonsCodecVersion")
                api("io.ktor:ktor-server-auth:$ktorVersion")
                api("io.ktor:ktor-server-call-logging:$ktorVersion")
                api("io.ktor:ktor-server-compression:$ktorVersion")
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-default-headers:$ktorVersion")
                api("io.ktor:ktor-server-netty:$ktorVersion")
                api("io.ktor:ktor-network-tls-certificates:$ktorVersion")
                api("io.ktor:ktor-server-auto-head-response:$ktorVersion")
                api("io.ktor:ktor-server-http-redirect:$ktorVersion")
                api("io.ktor:ktor-server-partial-content:$ktorVersion")
                api("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                api("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")
                api("org.litote.kmongo:kmongo-id-serialization:$kmongoVersion")
                api("org.jetbrains.exposed:exposed-core:$exposedVersion")
                api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$kotlinxDatetimeVersion")
                api("net.sourceforge.jtds:jtds:1.3.1")
                api("com.microsoft.sqlserver:mssql-jdbc:8.2.1.jre8")
            }
        }
        val backendTest by getting
        val frontendMain by getting {
            dependencies {
                api("org.litote.kmongo:kmongo-id:$kmongoVersion")
                api("io.kvision:kvision:$kvisionVersion")
                api("io.kvision:kvision-bootstrap:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                api("io.kvision:kvision-datetime:$kvisionVersion")
                api("io.kvision:kvision-chart:$kvisionVersion")
                api("io.kvision:kvision-fontawesome:$kvisionVersion")
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
            kotlin.srcDir("build/generated-src/frontend")
        }
        val frontendTest by getting
        val androidMain by getting
    }
}

android {
    namespace = "com.fonrouge.fsLib"
    compileSdk = 33
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
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
tasks.withType<KspTaskMetadata>() {
    dependsOn(tasks.getByPath(":compileReleaseKotlinAndroid"))
    dependsOn(tasks.getByPath(":compileDebugKotlinAndroid"))
    dependsOn(tasks.getByPath(":androidReleaseSourcesJar"))
    dependsOn(tasks.getByPath(":androidDebugSourcesJar"))
    dependsOn(tasks.getByPath(":backendSourcesJar"))
    dependsOn(tasks.getByPath(":sourcesJar"))
}
