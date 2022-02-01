plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
//    id("io.kvision") version kvisionVersion
    `maven-publish`
    `java-library`
}

group = "com.fonrouge.fslib"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kvisionVersion: String by System.getProperties()
val serializationVersion: String by project
val coroutinesVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project

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
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
            }
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
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-auth:$ktorVersion")
                implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.4.0")
            }
        }
        val backendTest by getting
        val frontendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime-js:0.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("io.kvision:kvision:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-datetime:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-select:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")
                implementation("io.kvision:kvision-rest:$kvisionVersion")
                implementation("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                implementation("io.kvision:kvision-tabulator:$kvisionVersion")
                implementation("io.kvision:kvision-tabulator-remote:$kvisionVersion")
                implementation("io.kvision:kvision-toast:$kvisionVersion")
            }
        }
        val frontendTest by getting
    }
}
