pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "fsLib"
include(":core")
include(":fullstack")
include(":mongodb")
include(":sql")
include(":media")
include(":ssr")
include(":memorydb")
include(":samples:fullstack:rpc-demo")
include(":samples:fullstack:greeting")
include(":samples:fullstack:contacts")
include(":samples:fullstack:showcase")
include(":samples:ssr:basic")
include(":samples:ssr:catalog")
include(":samples:ssr:advanced")
