package com.fonrouge.fullStack.services

/**
 * Registry for the client-side [IHelpDocsService] proxy.
 *
 * The help buttons system ([com.fonrouge.fullStack.layout.helpButtons]) reads from this
 * registry to make RPC calls. If no service is registered, the help "?" button
 * simply does not appear (no errors are thrown).
 *
 * Consumer applications must register their KSP-generated proxy during JS app startup:
 *
 * ```kotlin
 * import com.fonrouge.fullStack.services.HelpDocsServiceRegistry
 * import dev.kilua.rpc.getService
 *
 * HelpDocsServiceRegistry.service = getService<IHelpDocsService>()
 * ```
 *
 * This requires a `@RpcService`-annotated subclass of
 * [HelpDocsService][com.fonrouge.fullStack.services.HelpDocsService] on the server side
 * so that KSP generates the client proxy in the consumer project's scope.
 *
 * @see IHelpDocsService
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
