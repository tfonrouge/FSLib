package com.fonrouge.fslib

import com.fonrouge.fslib.security.Security
import com.fonrouge.fslib.services.PingService
import com.fonrouge.fslib.services.Profile
import com.fonrouge.fslib.services.ProfileService
import com.fonrouge.fslib.services.RegisterProfileService
import io.kvision.state.ObservableList
import io.kvision.state.observableListOf
import io.kvision.toast.Toast

object Model {

    private val pingService = PingService()
    private val profileService = ProfileService()
    private val registerProfileService = RegisterProfileService()

    val profile: ObservableList<Profile> = observableListOf(Profile())

    suspend fun ping(message: String) {
        Security.withAuth {
            val s = pingService.ping(message)
            Toast.info(s)
        }
    }

    suspend fun readProfile() {
        Security.withAuth {
            profile[0] = profileService.getProfile()
        }
    }

    suspend fun registerProfile(profile: Profile, password: String): Boolean {
        return try {
            registerProfileService.registerProfile(profile, password)
        } catch (e: Exception) {
            console.log(e)
            false
        }
    }
}
