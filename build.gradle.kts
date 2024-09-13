plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.serialization) apply false
//    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
    id("maven-publish")
}
