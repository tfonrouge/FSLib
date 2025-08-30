plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("maven-publish")
}

group = "com.fonrouge.fsLib"
version = libs.versions.fsLib.get()

repositories {
    mavenCentral()
}

kotlin {
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
            implementation(project(":base"))
            implementation(project(":fullStack"))
            implementation(libs.kotlinx.html)
            implementation(libs.kvision.common.remote)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.kmongo.coroutine.serialization)
            implementation(libs.ktor.server.sessions)
        }
        jsMain.dependencies {
            implementation(libs.kvision)
            implementation(libs.kvision.bootstrap)
            implementation(libs.kvision.bootstrap.upload)
            implementation(libs.kvision.tabulator)

            implementation(libs.kvision.react)
        }
    }
}
