package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpTheme
import kotlinx.browser.localStorage

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
 * // Optional: set an app-level default theme (used when the user has no stored preference)
 * HelpDocsServiceRegistry.defaultTheme = HelpTheme.DARK
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

    /** localStorage key used to persist the user's help theme preference. */
    private const val THEME_STORAGE_KEY = "fslib-help-theme"

    /**
     * The registered [IHelpDocsService] proxy instance.
     *
     * When `null`, the help buttons UI silently skips help discovery
     * (no errors are thrown, the "?" button simply does not appear).
     */
    var service: IHelpDocsService? = null

    /**
     * The app-level default theme, used when the user has no stored preference.
     *
     * Consumer apps set this at startup to define their preferred default.
     * If the user has previously toggled the theme (stored in localStorage),
     * the stored preference takes priority over this default.
     *
     * Defaults to [HelpTheme.AUTO].
     */
    var defaultTheme: HelpTheme = HelpTheme.AUTO

    /**
     * The color theme applied to help content.
     *
     * - [HelpTheme.AUTO]: Follows the user's OS/browser `prefers-color-scheme` preference.
     * - [HelpTheme.DARK]: Forces dark theme regardless of OS preference.
     * - [HelpTheme.LIGHT]: Forces light theme regardless of OS preference.
     *
     * On first access, the value is loaded from the browser's `localStorage`.
     * If no stored preference exists, [defaultTheme] is used.
     *
     * Use [persistThemeFromToggle] after assigning a new value to persist the user's
     * choice to localStorage. Direct assignment alone does **not** persist,
     * allowing consumer apps to set programmatic overrides without polluting storage.
     */
    var theme: HelpTheme = loadStoredTheme()

    /**
     * Persists the given [HelpTheme] to localStorage.
     *
     * Call this after setting [theme] from a user-initiated toggle action
     * so the preference survives page reloads. Consumer apps that set [theme]
     * programmatically at startup should **not** call this method — only
     * user-driven UI toggles should persist.
     *
     * @param helpTheme The theme to save.
     */
    fun persistThemeFromToggle(helpTheme: HelpTheme) {
        try {
            localStorage.setItem(THEME_STORAGE_KEY, helpTheme.cssValue)
        } catch (_: Exception) {
            // localStorage may be unavailable (e.g., private browsing quota exceeded)
        }
    }

    /**
     * Loads the persisted help theme from localStorage.
     *
     * @return The stored [HelpTheme], or [defaultTheme] if no valid value is found.
     */
    private fun loadStoredTheme(): HelpTheme {
        val stored = try {
            localStorage.getItem(THEME_STORAGE_KEY)
        } catch (_: Exception) {
            null
        }
        return stored?.let { css ->
            HelpTheme.entries.find { it.cssValue == css }
        } ?: defaultTheme
    }
}
