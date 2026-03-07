package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service for discovering and retrieving help documentation for views.
 *
 * Help files are organized under the configured help-docs directory.
 * When [moduleSlug] is provided, files are looked up in `help-docs/{moduleSlug}/{viewClassName}/`;
 * otherwise they fall back to `help-docs/{viewClassName}/`.
 */
@RpcService
interface IHelpDocsService {
    /**
     * Returns the set of available help types for the given view.
     *
     * @param viewClassName The simple class name of the view (e.g., "ViewOrdenTrabajo").
     * @param moduleSlug Optional module directory slug (e.g., "importaciones").
     *                   When provided, files are searched under `help-docs/{moduleSlug}/{viewClassName}/`.
     */
    suspend fun getAvailableHelp(viewClassName: String, moduleSlug: String? = null): Set<HelpType>

    /**
     * Fetches the raw HTML content for a specific help type and view.
     *
     * @param viewClassName The simple class name of the view.
     * @param helpType The type of help documentation to retrieve.
     * @param moduleSlug Optional module directory slug.
     * @return The HTML content as a string, or empty if not found.
     */
    suspend fun getHelpContent(viewClassName: String, helpType: HelpType, moduleSlug: String? = null): String
}
