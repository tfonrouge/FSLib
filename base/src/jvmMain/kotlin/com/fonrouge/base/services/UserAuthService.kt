package com.fonrouge.base.services

import com.fonrouge.base.model.UserSession
import dev.kilua.rpc.ServiceException
import io.ktor.server.application.*
import io.ktor.server.sessions.*

/**
 * Retrieves the user session of type [UID] associated with the current application call.
 *
 * @return The user session of type [UID] if available, or null if no session is associated with the call.
 */
inline fun <reified UID : Any> ApplicationCall.getUserSession(): UserSession<UID>? {
    return sessions.get()
}

/**
 * Retrieves the current session user of type [UID] from the application call.
 * If no valid user is found, an exception is thrown.
 *
 * @return The current session user of type [UID].
 * @throws Exception If a valid user is not found in the session.
 */
@Suppress("unused")
inline fun <reified UID : Any> ApplicationCall.requireUserSession(): UserSession<UID> {
    return sessions.get() ?: throw Exception("Valid user required")
}

/**
 * Sets the current user in the application session.
 *
 * @param UID The type of the user, which extends from `IUser`.
 * @param userSession The user instance to be set in the session.
 */
@Suppress("unused")
inline fun <reified UID : Any> ApplicationCall.setUserSession(userSession: UserSession<UID>) {
    sessions.set(userSession)
}

/**
 * Executes the given block of code with the user session of type [UID] associated with the current application call.
 * If no user session is available, a [ServiceException] is thrown.
 *
 * @param RESP The return type of the block to be executed.
 * @param UID The type of the user identifier.
 * @param block The block of code to be executed with the user session.
 * @return The result of the block execution.
 * @throws ServiceException If no user session is associated with the current application call.
 */
@Suppress("unused")
inline fun <RESP, reified UID : Any> ApplicationCall.withUser(block: (UserSession<UID>) -> RESP): RESP {
    return getUserSession<UID>()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}
