package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.base.IUser
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.kvision.remote.ServiceException

inline fun <reified U : IUser<*>> ApplicationCall.getUser(): U? {
    return sessions.get()
}

@Suppress("unused")
inline fun <reified U : IUser<*>> ApplicationCall.requireUser(): U {
    return sessions.get() ?: throw Exception("Valid user required")
}

@Suppress("unused")
inline fun <reified U : IUser<*>> ApplicationCall.setUser(user: U) {
    sessions.set(user)
}

@Suppress("unused")
inline fun <RESP, reified U : IUser<*>> ApplicationCall.withUser(block: (U) -> RESP): RESP {
    return getUser<U>()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}
