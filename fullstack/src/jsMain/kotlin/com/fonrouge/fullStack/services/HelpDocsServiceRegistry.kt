package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpTheme

/**
 * Registry for the client-side [IHelpDocsService] proxy and help system configuration.
 *
 * The help buttons system ([com.fonrouge.fullStack.layout.helpButtons]) reads from this
 * registry to make RPC calls and apply theme settings. If no service is registered,
 * the help "?" button simply does not appear (no errors are thrown).
 *
 * Consumer applications must register their KSP-generated proxy during JS app startup:
 *
 * ```kotlin
 * import com.fonrouge.fullStack.services.HelpDocsServiceRegistry
 * import dev.kilua.rpc.getService
 *
 * HelpDocsServiceRegistry.service = getService<IHelpDocsService>()
 *
 * // Optional: override the help theme (default is AUTO, which follows OS preference)
 * HelpDocsServiceRegistry.theme = HelpTheme.LIGHT
 * ```
 *
 * This requires a `@RpcService`-annotated subclass of
 * [HelpDocsService][com.fonrouge.fullStack.services.HelpDocsService] on the server side
 * so that KSP generates the client proxy in the consumer project's scope.
 *
 * @see IHelpDocsService
 * @see HelpTheme
 */
object HelpDocsServiceRegistry {

    /**
     * The registered [IHelpDocsService] proxy instance.
     *
     * When `null`, the help buttons UI silently skips help discovery
     * (no errors are thrown, the "?" button simply does not appear).
     */
    var service: IHelpDocsService? = null

    /**
     * The color theme applied to help content.
     *
     * - [HelpTheme.AUTO]: Follows the user's OS/browser `prefers-color-scheme` preference (default).
     * - [HelpTheme.DARK]: Forces dark theme regardless of OS preference.
     * - [HelpTheme.LIGHT]: Forces light theme regardless of OS preference.
     *
     * This value can be changed at any time; the next help panel opened will use the new theme.
     */
    var theme: HelpTheme = HelpTheme.AUTO
}
