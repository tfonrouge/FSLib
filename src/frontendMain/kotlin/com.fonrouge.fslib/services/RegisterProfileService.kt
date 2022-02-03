package com.fonrouge.fslib.services

import io.kvision.remote.KVRemoteAgent
import kotlinx.serialization.modules.SerializersModule
import org.w3c.fetch.RequestInit

actual class RegisterProfileService(
    serializersModules: List<SerializersModule>? = null,
    requestFilter: (RequestInit.() -> Unit)? = null
) : IRegisterProfileService,
    KVRemoteAgent<RegisterProfileService>(RegisterProfileServiceManager, serializersModules, requestFilter) {
    override suspend fun registerProfile(profile: Profile, password: String) =
        call(IRegisterProfileService::registerProfile, profile, password)
}
