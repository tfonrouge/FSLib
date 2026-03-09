package com.fonrouge.ssr.model

import kotlinx.serialization.Serializable

/**
 * A session-stored message displayed to the user after a redirect.
 * Follows the Post-Redirect-Get pattern for user feedback.
 */
@Serializable
data class FlashMessage(
    val level: Level,
    val message: String,
) {
    /**
     * Severity level for flash messages, maps to Bootstrap alert classes.
     */
    @Serializable
    enum class Level(val cssClass: String) {
        Success("alert-success"),
        Error("alert-danger"),
        Warning("alert-warning"),
        Info("alert-info"),
    }

    companion object {
        /** Creates a success flash message. */
        fun success(message: String) = FlashMessage(Level.Success, message)

        /** Creates an error flash message. */
        fun error(message: String) = FlashMessage(Level.Error, message)

        /** Creates a warning flash message. */
        fun warning(message: String) = FlashMessage(Level.Warning, message)

        /** Creates an info flash message. */
        fun info(message: String) = FlashMessage(Level.Info, message)
    }
}
