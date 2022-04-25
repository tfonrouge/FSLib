plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
    `maven-publish`
    `java-library`

//    id("org.jetbrains.dokka") version kotlinVersion

    signing
}

group = "com.fonrouge.fsLib"
version = "1.0.3-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
//    from(tasks.named("dokkaHtml"))
}

val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

publishing {

    // Configure all publications
    publications.withType<MavenPublication> {

        // Stub javadoc.jar artifact
        artifact(javadocJar)

        // Provide artifacts information requited by Maven Central
        pom {
            name.set(rootProject.name)
            description.set("Sample Kotlin Multiplatform library (jvm + ios + js) test")
            url.set("https://github.com/KaterinaPetrova/mpp-sample-lib")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("KaterinaPetrova")
                    name.set("Ekaterina Petrova")
                    email.set("ekaterina.petrova@jetbrains.com")
                }
            }
            scm {
                url.set("https://github.com/KaterinaPetrova/mpp-sample-lib")
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    val file = File("/Users/teo/teo_fonrouge_com_gpg.key")
    useInMemoryPgpKeys(
        file.readText(),
        System.getenv("GPG_PRIVATE_PASSWORD")
    )
    sign(publishing.publications)
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
                api("io.kvision:kvision-bootstrap-css:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-datetime:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-dialog:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-select:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-spinner:$kvisionVersion")
                api("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                api("io.kvision:kvision-datacontainer:$kvisionVersion")
                api("io.kvision:kvision-fontawesome:$kvisionVersion")
                api("io.kvision:kvision-react:$kvisionVersion")
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
