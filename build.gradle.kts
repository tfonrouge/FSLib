plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.kilua.rpc) apply false
}

// ---------------------------------------------------------------------------
// Central Portal bundle upload task
// ---------------------------------------------------------------------------
// Publishes all staged artifacts to Maven Central via the Central Portal API.
//
// Full workflow:
//   1. ./gradlew publishAllPublicationsToStagingRepository
//   2. ./gradlew publishToCentralPortal
// ---------------------------------------------------------------------------

tasks.register("publishToCentralPortal", Exec::class) {
    description = "Uploads the staging-deploy bundle to Maven Central Portal"
    group = "publishing"

    val stagingDir = layout.buildDirectory.dir("staging-deploy")
    val bundleFile = layout.buildDirectory.file("central-bundle.zip")
    val username = providers.gradleProperty("ossrhUsername")
    val password = providers.gradleProperty("ossrhPassword")

    inputs.dir(stagingDir)
    outputs.file(bundleFile)

    doFirst {
        val staging = stagingDir.get().asFile
        if (!staging.exists() || staging.listFiles()?.isEmpty() != false) {
            error("No staged artifacts found. Run publishAllPublicationsToStagingRepository first.")
        }

        // Create ZIP bundle from staging directory
        ant.withGroovyBuilder {
            "zip"("destfile" to bundleFile.get().asFile, "basedir" to staging)
        }

        val user = username.getOrElse("")
        val pass = password.getOrElse("")
        if (user.isBlank() || pass.isBlank()) {
            error("ossrhUsername/ossrhPassword not set in ~/.gradle/gradle.properties")
        }

        val authToken = java.util.Base64.getEncoder()
            .encodeToString("$user:$pass".toByteArray())

        commandLine(
            "curl", "-s", "-w", "\n%{http_code}",
            "--fail-with-body",
            "-X", "POST",
            "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC",
            "-H", "Authorization: UserToken $authToken",
            "-F", "bundle=@${bundleFile.get().asFile.absolutePath}"
        )
    }
}
