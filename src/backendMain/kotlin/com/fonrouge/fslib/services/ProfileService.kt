package com.fonrouge.fslib.services

import com.google.inject.Inject
import io.ktor.application.*

actual class ProfileService : IProfileService {

    @Inject
    lateinit var call: ApplicationCall

    override suspend fun getProfile() = call.withProfile { it }
}
