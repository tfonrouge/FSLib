package com.fonrouge.ssr.session

import com.fonrouge.ssr.model.FlashMessage
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

/**
 * Session data container for flash messages.
 * Stored in the Ktor session and consumed on the next page render.
 */
@Serializable
data class FlashSession(
    val messages: List<FlashMessage> = emptyList(),
)

/**
 * Adds a flash message to the current session.
 * The message will be displayed on the next rendered page and then cleared.
 */
fun ApplicationCall.addFlash(message: FlashMessage) {
    try {
        val current = sessions.get<FlashSession>() ?: FlashSession()
        sessions.set(current.copy(messages = current.messages + message))
    } catch (_: Exception) {
        // Session not available (e.g. Sessions plugin not installed or no transport configured)
    }
}

/**
 * Adds a flash message with the given level and text.
 */
fun ApplicationCall.addFlash(level: FlashMessage.Level, text: String) {
    addFlash(FlashMessage(level, text))
}

/**
 * Retrieves and clears all flash messages from the session.
 * Returns an empty list if no messages are pending.
 */
fun ApplicationCall.consumeFlash(): List<FlashMessage> {
    return try {
        val session = sessions.get<FlashSession>() ?: return emptyList()
        sessions.clear<FlashSession>()
        session.messages
    } catch (_: Exception) {
        emptyList()
    }
}
