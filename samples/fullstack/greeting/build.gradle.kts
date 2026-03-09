import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kilua.rpc)
    alias(libs.plugins.kvision)
}

version = "1.0.0-SNAPSHOT"
group = "com.example"

val mainClassName = "io.ktor.server.netty.EngineMain"

kotlin {
    jvmToolchain(21)
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set(mainClassName)
        }
    }
    js(IR) {
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":fullstack"))
                implementation(libs.kilua.rpc.ktor)
                implementation(libs.kvision.common.remote)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.compression)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kvision)
                implementation(libs.kvision.bootstrap)
                implementation(libs.kvision.fontawesome)
                implementation(libs.kvision.toastify)
                implementation(libs.kvision.tabulator.remote)
                implementation(libs.kvision.material)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation(libs.kvision.testutils)
            }
        }
    }
}
