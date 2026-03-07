package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import io.ktor.server.application.*
import java.io.File

/**
 * Service for managing help documents associated with application views.
 *
 * Provides methods to query the availability and content of help files organized
 * by view class name and help type ([HelpType]).
 *
 * @property call The Ktor HTTP call associated with the current request.
 */
@Suppress("unused")
class HelpDocsService(val call: ApplicationCall) : IHelpDocsService {
    companion object {
        private var helpDocsDir: File = File("help-docs")

        /**
         * Sets the root directory where help documents are stored.
         *
         * @param dir Path to the help documents directory.
         */
        fun setHelpDocsDir(dir: String) {
            helpDocsDir = File(dir)
        }
    }

    /**
     * Gets the available help types for a specific view.
     *
     * Searches the directory corresponding to [viewClassName] for existing help files
     * and returns the set of found types.
     *
     * @param viewClassName Name of the view class to query help for.
     * @return Set of [HelpType] whose files exist in the view's directory.
     */
    override suspend fun getAvailableHelp(viewClassName: String): Set<HelpType> {
        val viewDir = File(helpDocsDir, viewClassName)
        return HelpType.entries.filter { File(viewDir, it.fileName).exists() }.toSet()
    }

    /**
     * Gets the content of a specific help document.
     *
     * @param viewClassName Name of the view class associated with the document.
     * @param helpType The requested help type.
     * @return Content of the help file as text, or an empty string if the file does not exist.
     */
    override suspend fun getHelpContent(viewClassName: String, helpType: HelpType): String {
        val file = File(helpDocsDir, "$viewClassName/${helpType.fileName}")
        return if (file.exists()) file.readText() else ""
    }

    init {
        helpDocsDir.mkdirs()
    }
}
