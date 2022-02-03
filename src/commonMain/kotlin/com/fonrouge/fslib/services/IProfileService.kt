package com.fonrouge.fslib.services

import io.kvision.annotations.KVService

@KVService
interface IProfileService {
    suspend fun getProfile(): Profile
}
