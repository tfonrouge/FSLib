package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import io.ktor.server.application.*
import java.io.File

/**
 * Abstract server-side implementation of [IHelpDocsService].
 *
 * Provides file-based help document resolution organized by view class name,
 * optional module slug, and help type ([HelpType]).
 *
 * When a [moduleSlug] is provided, files are first looked up under
 * `helpDocsDir/{moduleSlug}/{viewClassName}/`. If not found, the service falls back
 * to the flat layout `helpDocsDir/{viewClassName}/` for backward compatibility.
 *
 * ## Usage
 *
 * Consumer applications must create a concrete subclass annotated with `@RpcService`
 * and register it in their Kilua RPC initialization:
 *
 * ```kotlin
 * @RpcService
 * class MyHelpDocsService(call: ApplicationCall) : HelpDocsService(call)
 *
 * // in initRpc:
 * registerService<IHelpDocsService> { MyHelpDocsService(it) }
 * ```
 *
 * The subclass body can be empty — all logic is provided by this abstract class.
 * The `@RpcService` annotation must be on the consumer's class so that KSP generates
 * the RPC proxy in the consumer's scope (not in the library's).
 *
 * @property call The Ktor HTTP call associated with the current request.
 * @see IHelpDocsService
 * @see HelpDocsServiceRegistry
 */
@Suppress("unused")
abstract class HelpDocsService(val call: ApplicationCall) : IHelpDocsService {

    /**
     * Resolves the view directory, trying the module-scoped path first
     * and falling back to the flat path.
     *
     * @param viewClassName Name of the view class.
     * @param moduleSlug Optional module directory slug.
     * @return The resolved [File] directory (may or may not exist).
     */
    private fun resolveViewDir(viewClassName: String, moduleSlug: String?): File {
        if (moduleSlug != null) {
            val moduleDir = File(helpDocsDir, "$moduleSlug/$viewClassName")
            if (moduleDir.exists()) return moduleDir
        }
        return File(helpDocsDir, viewClassName)
    }

    /**
     * Resolves the file for a given help type.
     *
     * - [HelpType.MANUAL]: looked up at module level `helpDocsDir/{moduleSlug}/manual.html`
     *   (requires [moduleSlug]; returns `null` if not provided).
     * - [HelpType.TUTORIAL], [HelpType.CONTEXT_HELP]: looked up at view level via [resolveViewDir].
     *
     * @param viewClassName Name of the view class.
     * @param helpType The requested help type.
     * @param moduleSlug Optional module directory slug.
     * @return The resolved [File], or `null` if a manual is requested without a module slug.
     */
    private fun resolveHelpFile(viewClassName: String, helpType: HelpType, moduleSlug: String?): File? {
        return if (helpType == HelpType.MANUAL) {
            if (moduleSlug != null) File(helpDocsDir, "$moduleSlug/${helpType.fileName}") else null
        } else {
            File(resolveViewDir(viewClassName, moduleSlug), helpType.fileName)
        }
    }

    /**
     * Gets the available help types for a specific view.
     *
     * Searches for per-view help files (tutorial, context) in the view directory,
     * and for a module-level manual in the module directory when [moduleSlug] is provided.
     *
     * @param viewClassName Name of the view class to query help for.
     * @param moduleSlug Optional module directory slug (e.g., "importaciones").
     * @return Set of [HelpType] whose files exist.
     */
    override suspend fun getAvailableHelp(viewClassName: String, moduleSlug: String?): Set<HelpType> {
        return HelpType.entries.filter { helpType ->
            resolveHelpFile(viewClassName, helpType, moduleSlug)?.exists() == true
        }.toSet()
    }

    /**
     * Gets the content of a specific help document.
     *
     * For [HelpType.MANUAL], reads from `helpDocsDir/{moduleSlug}/manual.html`.
     * For other types, reads from the resolved view directory.
     *
     * After reading the file, any `<!-- include: filename.html -->` directives are resolved
     * by loading the referenced file relative to the same directory. This enables shared
     * HTML fragments (e.g., `_fields.html`) to be reused across multiple help documents
     * without client-side fetch logic.
     *
     * @param viewClassName Name of the view class associated with the document.
     * @param helpType The requested help type.
     * @param moduleSlug Optional module directory slug.
     * @return Content of the help file as text (with includes resolved), or an empty string if the file does not exist.
     */
    override suspend fun getHelpContent(viewClassName: String, helpType: HelpType, moduleSlug: String?): String {
        val file = resolveHelpFile(viewClassName, helpType, moduleSlug) ?: return ""
        if (!file.exists()) return ""
        return resolveIncludes(file)
    }

    /**
     * Resolves `<!-- include: filename -->` directives in an HTML file by replacing each
     * directive with the content of the referenced file.
     *
     * Include paths are resolved relative to the directory of the file containing the directive.
     * Paths may use `../` to reference files in sibling directories (e.g., `../ViewItemEntity/_fields.html`).
     * Missing files are replaced with an HTML comment indicating the error.
     * Nested includes (includes within included files) are supported up to a reasonable depth.
     *
     * @param file The HTML file to process.
     * @param depth Current recursion depth to prevent infinite loops (max 5 levels).
     * @return The file content with all include directives resolved.
     */
    private fun resolveIncludes(file: File, depth: Int = 0): String {
        if (depth > 5) return file.readText()
        val content = file.readText()
        return includeRegex.replace(content) { match ->
            val includePath = match.groupValues[1].trim()
            val includeFile = File(file.parentFile, includePath).canonicalFile
            if (includeFile.exists() && includeFile.startsWith(helpDocsDir.canonicalFile)) {
                resolveIncludes(includeFile, depth + 1)
            } else {
                "<!-- include not found: $includePath -->"
            }
        }
    }

    companion object {
        private var helpDocsDir: File = File("help-docs")
        private val includeRegex = Regex("""<!--\s*include:\s*(.+?)\s*-->""")

        /**
         * Sets the root directory where help documents are stored.
         *
         * @param dir Path to the help documents directory.
         */
        fun setHelpDocsDir(dir: String) {
            helpDocsDir = File(dir)
        }
    }

    init {
        helpDocsDir.mkdirs()
    }
}
