package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service for discovering and retrieving help documentation for views.
 */
@RpcService
interface IHelpDocsService {
    /**
     * Returns the set of available help types for the given view.
     *
     * @param viewClassName The simple class name of the view (e.g., "ViewOrdenTrabajo").
     */
    suspend fun getAvailableHelp(viewClassName: String): Set<HelpType>

    /**
     * Fetches the raw HTML content for a specific help type and view.
     *
     * @param viewClassName The simple class name of the view.
     * @param helpType The type of help documentation to retrieve.
     * @return The HTML content as a string, or empty if not found.
     */
    suspend fun getHelpContent(viewClassName: String, helpType: HelpType): String
}
