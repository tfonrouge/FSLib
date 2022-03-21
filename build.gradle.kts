plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
    `maven-publish`
    `java-library`
}

group = "com.fonrouge.fsLib"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}

val kvisionVersion: String by System.getProperties()
val serializationVersion: String by project
val coroutinesVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project
val commonsCodecVersion: String by project

val webDir = file("src/frontendMain/web")
val mainClassName = "io.ktor.server.netty.EngineMain"

kotlin {
    jvm("backend") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js("frontend", IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.library()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.kvision:kvision-server-ktor:$kvisionVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
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
//                implementation("org.jetbrains.exposed:exposed:$exposedVersion")
                api("commons-codec:commons-codec:$commonsCodecVersion")
                api("io.ktor:ktor-auth:$ktorVersion")
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-netty:$ktorVersion")
                api("org.litote.kmongo:kmongo-coroutine-serialization:4.4.0")
            }
        }
        val backendTest by getting
        val frontendMain by getting {
            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-datetime-js:0.2.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                api("io.kvision:kvision:$kvisionVersion")
                api("io.kvision:kvision-bootstrap:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-datetime:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-dialog:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-select:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                api("io.kvision:kvision-redux-kotlin:$kvisionVersion")
                api("io.kvision:kvision-rest:$kvisionVersion")
                api("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                api("io.kvision:kvision-tabulator:$kvisionVersion")
                api("io.kvision:kvision-tabulator-remote:$kvisionVersion")
                api("io.kvision:kvision-toast:$kvisionVersion")
            }
            kotlin.srcDir("build/generated-src/frontend")
        }
        val frontendTest by getting
    }
}
