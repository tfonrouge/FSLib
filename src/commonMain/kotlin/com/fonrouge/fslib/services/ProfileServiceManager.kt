package com.fonrouge.fslib.services

import io.kvision.remote.HttpMethod
import io.kvision.remote.KVServiceManager

expect class ProfileService : IProfileService

object ProfileServiceManager : KVServiceManager<ProfileService>(ProfileService::class) {
    init {
        bind(IProfileService::getProfile, HttpMethod.POST, null)
    }
}
