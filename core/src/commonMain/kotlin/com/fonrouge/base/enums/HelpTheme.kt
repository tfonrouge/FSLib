package com.fonrouge.base.enums

/**
 * Defines the available color themes for help documentation.
 *
 * Used by [com.fonrouge.fullStack.services.HelpDocsServiceRegistry] to control
 * the appearance of help content (tutorials, context help, and manuals).
 *
 * @property cssValue The value used in the `data-help-theme` HTML attribute.
 * @property icon A Unicode glyph representing this theme in the UI.
 * @property label A short human-readable label for this theme.
 */
enum class HelpTheme(val cssValue: String, val icon: String, val label: String) {
    /** Automatically follows the user's OS/browser color scheme preference. */
    AUTO("auto", "\u25D0", "Auto (OS)"),

    /** Forces the dark color scheme regardless of OS preference. */
    DARK("dark", "\u263E", "Dark"),

    /** Forces the light color scheme regardless of OS preference. */
    LIGHT("light", "\u2600", "Light")
}
