package com.fonrouge.fslib.services

import io.kvision.annotations.KVService

@KVService
interface IRegisterProfileService {
    suspend fun registerProfile(profile: Profile, password: String): Boolean
}
