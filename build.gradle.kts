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

// Staging accumulates across releases (module publications APPEND into staging-deploy), and the
// portal upload zips the WHOLE directory — so a leftover prior release gets re-submitted and every
// one of its components is rejected by Central as already existing, failing the entire deployment
// (including the genuinely new version riding in the same bundle). This bit twice as a forgotten
// manual step (6.2.2 → 6.2.3 residue, then 6.2.3 → 6.2.4); these two guards retire it:
// `cleanStagingDeploy` runs before any staging publication, and the upload refuses a mixed bundle.
val cleanStagingDeploy = tasks.register("cleanStagingDeploy", Delete::class) {
    description = "Empties staging-deploy so a release bundle can only contain the version being staged"
    group = "publishing"
    delete(layout.buildDirectory.dir("staging-deploy"), layout.buildDirectory.file("central-bundle.zip"))
}

// The publication inventory every complete release bundle must carry, derived at configuration
// time from the modules that apply the `fslib-publishing` convention (so a new published module —
// or a retired one — updates the expectation without touching this file). KMP modules contribute
// their root + per-target publications; `:conformance` applies no publishing and never appears.
val expectedPublicationIds = objects.setProperty(String::class)
gradle.projectsEvaluated {
    expectedPublicationIds.set(
        subprojects
            .filter { it.plugins.hasPlugin("fslib-publishing") }
            .flatMap { sp ->
                sp.extensions.findByType(org.gradle.api.publish.PublishingExtension::class.java)
                    ?.publications
                    ?.withType(org.gradle.api.publish.maven.MavenPublication::class.java)
                    ?.map { it.artifactId }
                    ?: emptyList()
            }
            .toSet()
    )
    expectedPublicationIds.finalizeValue()
}

// Preflight for the Central Portal upload (ACS-06, blueprints/view-consumer-gaps LEDGER L-010).
// The mixed-bundle guard alone accepted any SINGLE version — including a stale bundle from a
// previous release, or a partial bundle from a module-scoped staging run. This task additionally
// pins the staged version to the version catalog and the staged artifact set to the expected
// publication inventory, and runs standalone so a release can verify the bundle without uploading.
val verifyStagingDeploy = tasks.register("verifyStagingDeploy") {
    description = "Verifies staging-deploy holds exactly the catalog version and the full publication inventory"
    group = "publishing"

    val stagingDir = layout.buildDirectory.dir("staging-deploy")
    val expectedVersion = libs.versions.fsLib.get()
    val expectedIds = expectedPublicationIds

    // Re-verify every run: the staged tree can change without any task input Gradle tracks here.
    outputs.upToDateWhen { false }

    doLast {
        val staging = stagingDir.get().asFile
        if (!staging.exists() || staging.listFiles()?.isEmpty() != false) {
            error("No staged artifacts found. Run publishAllPublicationsToStagingRepository first.")
        }

        val poms = staging.walkTopDown().filter { it.isFile && it.extension == "pom" }.toList()

        // Refuse a mixed bundle: Central rejects any component whose version already exists, and
        // one rejected component fails the whole deployment — taking the new release down with it.
        val stagedVersions = poms.mapNotNull { it.parentFile?.name }.toSortedSet()
        if (stagedVersions.size != 1) {
            error(
                "staging-deploy contains ${stagedVersions.size} versions: $stagedVersions. " +
                    "A bundle must carry exactly one. Run ./gradlew cleanStagingDeploy " +
                    "publishAllPublicationsToStagingRepository and retry."
            )
        }

        // Refuse a stale single-version bundle: one left over from a previous release passes the
        // mixed-bundle guard while re-submitting a version Central already has.
        val stagedVersion = stagedVersions.single()
        if (stagedVersion != expectedVersion) {
            error(
                "staging-deploy holds version $stagedVersion but the version catalog says " +
                    "$expectedVersion. Run ./gradlew cleanStagingDeploy " +
                    "publishAllPublicationsToStagingRepository and retry."
            )
        }

        // Refuse a partial bundle: module-scoped staging cleans the shared directory first, so a
        // bundle staged that way carries a subset that would publish an incomplete release.
        val stagedIds = poms.mapNotNull { it.parentFile?.parentFile?.name }.toSortedSet()
        val expected = expectedIds.get().toSortedSet()
        if (stagedIds != expected) {
            val missing = expected - stagedIds
            val unexpected = stagedIds - expected
            error(
                buildString {
                    append("staging-deploy does not match the expected publication inventory.")
                    if (missing.isNotEmpty()) append(" Missing: $missing.")
                    if (unexpected.isNotEmpty()) append(" Unexpected: $unexpected.")
                    append(
                        " Run ./gradlew cleanStagingDeploy publishAllPublicationsToStagingRepository " +
                            "and retry."
                    )
                }
            )
        }

        logger.lifecycle(
            "staging-deploy verified: version $stagedVersion, ${stagedIds.size} publications " +
                "matching the expected inventory."
        )
    }
}

tasks.register("publishToCentralPortal", Exec::class) {
    description = "Uploads the staging-deploy bundle to Maven Central Portal"
    group = "publishing"
    dependsOn(verifyStagingDeploy)

    val stagingDir = layout.buildDirectory.dir("staging-deploy")
    val bundleFile = layout.buildDirectory.file("central-bundle.zip")
    val username = providers.gradleProperty("ossrhUsername")
    val password = providers.gradleProperty("ossrhPassword")

    inputs.dir(stagingDir)
    outputs.file(bundleFile)

    doFirst {
        val staging = stagingDir.get().asFile

        // Create ZIP bundle from staging directory (content validated by verifyStagingDeploy)
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
