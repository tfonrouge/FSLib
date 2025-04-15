package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.base.IUser
import dev.kilua.rpc.ServiceException
import io.ktor.server.application.*
import io.ktor.server.sessions.*

/**
 * Retrieves the current user of type [U] from the application call's session.
 *
 * @return The current user of type [U] if available, or null if no user is set in the session.
 */
inline fun <reified U : IUser<*>> ApplicationCall.getUser(): U? {
    return sessions.get()
}

/**
 * Retrieves the current session user of type [U] from the application call.
 * If no valid user is found, an exception is thrown.
 *
 * @return The current session user of type [U].
 * @throws Exception If a valid user is not found in the session.
 */
@Suppress("unused")
inline fun <reified U : IUser<*>> ApplicationCall.requireUser(): U {
    return sessions.get() ?: throw Exception("Valid user required")
}

/**
 * Sets the current user in the application session.
 *
 * @param U The type of the user, which extends from `IUser`.
 * @param user The user instance to be set in the session.
 */
@Suppress("unused")
inline fun <reified U : IUser<*>> ApplicationCall.setUser(user: U) {
    sessions.set(user)
}

/**
 * Executes the provided block with the current user of type [U] retrieved from the application call's session.
 * If no user is set in the session, a `ServiceException` is thrown.
 *
 * @param RESP The return type of the block to be executed.
 * @param U The type of the user implementing `IUser`.
 * @param block A lambda function that takes a user of type [U] and returns a value of type [RESP].
 * @return The result of executing the block with the user of type [U].
 * @throws ServiceException If no user is set in the application call's session.
 */
@Suppress("unused")
inline fun <RESP, reified U : IUser<*>> ApplicationCall.withUser(block: (U) -> RESP): RESP {
    return getUser<U>()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}
