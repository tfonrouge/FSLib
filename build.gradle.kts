plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
    `maven-publish`
    `java-library`

//    id("org.jetbrains.dokka") version kotlinVersion

//    signing
}

group = "com.fonrouge.fsLib"
version = "1.4.0"

repositories {
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
//                api("io.kvision:kvision-bootstrap-css:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-datetime:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-dialog:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-select:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-select-remote:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-spinner:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-typeahead:$kvisionVersion")
//                api("io.kvision:kvision-bootstrap-typeahead-remote:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                api("io.kvision:kvision-datetime:$kvisionVersion")
                api("io.kvision:kvision-chart:$kvisionVersion")
//                api("io.kvision:kvision-datacontainer:$kvisionVersion")
                api("io.kvision:kvision-fontawesome:$kvisionVersion")
                api("io.kvision:kvision-jquery:$kvisionVersion")
//                api("io.kvision:kvision-moment:$kvisionVersion")
                api("io.kvision:kvision-pace:$kvisionVersion")
                api("io.kvision:kvision-print:$kvisionVersion")
                api("io.kvision:kvision-react:$kvisionVersion")
                api("io.kvision:kvision-redux-kotlin:$kvisionVersion")
                api("io.kvision:kvision-rest:$kvisionVersion")
                api("io.kvision:kvision-richtext:$kvisionVersion")
                api("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                api("io.kvision:kvision-tabulator:$kvisionVersion")
                api("io.kvision:kvision-tabulator-remote:$kvisionVersion")
//                api("io.kvision:kvision-toast:$kvisionVersion")
                api("io.kvision:kvision-toastify:$kvisionVersion")
                api("io.kvision:kvision-tom-select:$kvisionVersion")
                api("io.kvision:kvision-tom-select-remote:$kvisionVersion")
            }
            kotlin.srcDir("build/generated-src/frontend")
        }
        val frontendTest by getting
    }
}
