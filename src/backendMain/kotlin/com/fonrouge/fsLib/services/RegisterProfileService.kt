package com.fonrouge.fsLib.services

actual class RegisterProfileService : IRegisterProfileService {
    override suspend fun registerProfile(profile: Profile, password: String): Boolean {
        TODO("Not yet implemented")
    }
}
