package com.fonrouge.fslib.services

import io.ktor.application.*
import io.ktor.sessions.*

suspend fun <RESP> ApplicationCall.withProfile(block: suspend (Profile) -> RESP): RESP {
    val profile = this.sessions.get<Profile>()
    return profile?.let {
        block(profile)
    } ?: throw IllegalStateException("User Profile not set!")
}
