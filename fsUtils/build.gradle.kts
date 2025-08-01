plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("maven-publish")
}

group = "com.fonrouge.fsUtils"
version = libs.versions.fsLib.get()

repositories {
    mavenCentral()
    mavenLocal()
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
            implementation(project(":fsLib"))
            implementation(project(":modelUtils"))
            implementation(project(":backendLib"))
            implementation(libs.kotlinx.html)
        }
        jvmMain.dependencies {

        }
        jsMain.dependencies {

        }
    }
}
