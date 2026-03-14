// ---------------------------------------------------------------------------
// Convention plugin: Named Routes for Kilua RPC
// ---------------------------------------------------------------------------
// Applies to modules that use the Kilua RPC KSP processor (@RpcService).
//
// After KSP generates ServiceManager objects with `bind(fn, method, null)`,
// this plugin post-processes the generated code to replace `null` with
// explicit route names in the format "ServiceName.methodName".
//
// This produces human-readable Ktor route paths like:
//   /rpc/ITaskService.apiList
// instead of counter-based paths like:
//   /rpc/routeTaskServiceManager0
//
// Usage:
//   plugins {
//       id("fslib-named-routes")
//   }
// ---------------------------------------------------------------------------

abstract class PatchKspNamedRoutesTask : DefaultTask() {

    @get:InputDirectory
    @get:Optional
    abstract val generatedDir: DirectoryProperty

    @TaskAction
    fun patch() {
        val dir = generatedDir.orNull?.asFile ?: return
        if (!dir.exists()) return

        dir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filter { it.readText().contains("RpcServiceManager<") }
            .forEach { file ->
                val original = file.readText()
                val patched = patchBindCalls(original)
                if (patched != original) {
                    file.writeText(patched)
                    logger.lifecycle("Named routes patched: ${file.name}")
                }
            }
    }

    /**
     * Replaces `bind(IFoo::bar, HttpMethod.POST, null)` with
     * `bind(IFoo::bar, HttpMethod.POST, "IFoo.bar")` in KSP-generated code.
     */
    private fun patchBindCalls(source: String): String {
        val bindPattern = Regex(
            """(bind\()(\w+)::(\w+)(,\s*HttpMethod\.\w+),\s*null\)"""
        )
        return bindPattern.replace(source) { match ->
            val prefix = match.groupValues[1]
            val interfaceName = match.groupValues[2]
            val methodName = match.groupValues[3]
            val httpMethod = match.groupValues[4]
            """${prefix}${interfaceName}::${methodName}${httpMethod}, "${interfaceName}.${methodName}")"""
        }
    }
}

val patchKspNamedRoutes by tasks.registering(PatchKspNamedRoutesTask::class) {
    group = "kilua rpc"
    description = "Replaces null route params in KSP-generated ServiceManager bind() calls with explicit method names"
    generatedDir.set(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
}

// Wire into the build: run after KSP, before compilation
tasks.matching { it.name == "kspCommonMainKotlinMetadata" }.configureEach {
    finalizedBy(patchKspNamedRoutes)
}

tasks.matching { it.name == "compileCommonMainKotlinMetadata" }.configureEach {
    mustRunAfter(patchKspNamedRoutes)
}
