plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    id("com.android.library") version "8.2.2" apply false
    kotlin("multiplatform") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
//    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
    id("maven-publish")
}
