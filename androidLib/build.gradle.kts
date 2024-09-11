plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization")
    `maven-publish`
}

android {
    namespace = "com.fonrouge.androidLib"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
//        compose = true
    }
}

dependencies {
    implementation(project(":fsLib"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.animation.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material.icons.core.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.paging.compose.android)
    implementation(libs.eu.bambooapps.compose.material3.pullrefresh)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.code.scanner)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
}

publishing {
    publications {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.fonrouge.androidLib"
                artifactId = "androidLib"
                version = libs.versions.libVersion.get()

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}
