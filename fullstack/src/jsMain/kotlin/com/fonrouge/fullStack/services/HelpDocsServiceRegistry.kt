package com.fonrouge.fullStack.services

/**
 * Registry for the client-side [IHelpDocsService] proxy.
 *
 * Consumer applications must register their KSP-generated service instance
 * during initialization so the help buttons system can make RPC calls.
 *
 * Example:
 * ```kotlin
 * HelpDocsServiceRegistry.service = getService<IHelpDocsService>()
 * ```
 */
object HelpDocsServiceRegistry {

    /**
     * The registered [IHelpDocsService] proxy instance.
     *
     * When `null`, the help buttons UI silently skips help discovery
     * (no errors are thrown, the "?" button simply does not appear).
     */
    var service: IHelpDocsService? = null
}
