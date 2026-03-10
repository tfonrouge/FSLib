package com.fonrouge.base.enums

/**
 * Defines the available color themes for help documentation.
 *
 * Used by [com.fonrouge.fullStack.services.HelpDocsServiceRegistry] to control
 * the appearance of help content (tutorials, context help, and manuals).
 *
 * @property cssValue The value used in the `data-help-theme` HTML attribute.
 */
enum class HelpTheme(val cssValue: String) {
    /** Automatically follows the user's OS/browser color scheme preference. */
    AUTO("auto"),

    /** Forces the dark color scheme regardless of OS preference. */
    DARK("dark"),

    /** Forces the light color scheme regardless of OS preference. */
    LIGHT("light")
}
