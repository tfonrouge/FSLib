// ---------------------------------------------------------------------------
// Convention plugin: Maven Central publishing for FSLib modules
// ---------------------------------------------------------------------------
// Apply this plugin in any module that should be published to Maven Central.
//
// Credentials are read from ~/.gradle/gradle.properties:
//   ossrhUsername=<your Sonatype Central Portal token username>
//   ossrhPassword=<your Sonatype Central Portal token password>
//
// GPG signing uses the system gpg command (supports ed25519 keys).
// Configure in ~/.gradle/gradle.properties:
//   signing.gnupg.keyName=<GPG key ID, e.g. A80F810E>
//   signing.gnupg.passphrase=<GPG key passphrase>
//
// Usage:
//   ./gradlew publishAllPublicationsToStagingRepository  (all modules)
//   ./gradlew :core:publishAllPublicationsToStagingRepository  (single module)
//   Then from root: ./gradlew publishToCentralPortal
//
// Local development:
//   ./gradlew publishToMavenLocal -PSNAPSHOT
//   Appends "-SNAPSHOT" to the version automatically.
// ---------------------------------------------------------------------------

plugins {
    `maven-publish`
    signing
}

// Maven Central requires javadoc and sources JARs for all publications.
// KMP modules produce these automatically; JVM-only modules need explicit config.
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// For JVM-only modules (kotlin("jvm")), register a sources JAR from the main source set.
plugins.withId("org.jetbrains.kotlin.jvm") {
    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }
    publishing {
        publications.configureEach {
            if (this is MavenPublication && name == "maven") {
                artifact(sourcesJar)
            }
        }
    }
}

publishing {
    publications.configureEach {
        if (this is MavenPublication && (name == "jvm" || name == "maven")) {
            artifact(javadocJar)
        }
    }
}

// Append "-SNAPSHOT" to the version when the -PSNAPSHOT flag is passed.
// Usage: ./gradlew publishToMavenLocal -PSNAPSHOT
if (hasProperty("SNAPSHOT") && !version.toString().endsWith("-SNAPSHOT")) {
    version = "${version}-SNAPSHOT"
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("FSLib ${project.name}")
            description.set(
                "Kotlin Multiplatform library for full-stack web applications " +
                    "with MongoDB/SQL backends and KVision frontend"
            )
            url.set("https://github.com/tfonrouge/FSLib")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("tfonrouge")
                    name.set("Teo Fonrouge")
                    url.set("https://github.com/tfonrouge")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/tfonrouge/FSLib.git")
                developerConnection.set("scm:git:ssh://github.com/tfonrouge/FSLib.git")
                url.set("https://github.com/tfonrouge/FSLib")
            }
        }
    }

    repositories {
        maven {
            name = "Staging"
            url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    // Use system gpg command (required for ed25519 keys)
    useGpgCmd()
    // Only require signing when publishing to staging (not for publishToMavenLocal)
    isRequired = findProperty("signing.gnupg.keyName") != null
    sign(publishing.publications)
}