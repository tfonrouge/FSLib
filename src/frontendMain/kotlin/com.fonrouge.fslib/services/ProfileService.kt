package com.fonrouge.fslib.services

import io.kvision.remote.KVRemoteAgent
import kotlinx.serialization.modules.SerializersModule
import org.w3c.fetch.RequestInit

actual class ProfileService(
    serializersModules: List<SerializersModule>? = null,
    requestFilter: (RequestInit.() -> Unit)? = null
) : IProfileService, KVRemoteAgent<ProfileService>(ProfileServiceManager, serializersModules, requestFilter) {
    override suspend fun getProfile() = call(IProfileService::getProfile)
}
