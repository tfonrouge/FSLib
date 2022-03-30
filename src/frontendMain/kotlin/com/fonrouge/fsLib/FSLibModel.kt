package com.fonrouge.fsLib

import com.fonrouge.fsLib.security.Security
import com.fonrouge.fsLib.services.Profile
import com.fonrouge.fsLib.services.ProfileService
import com.fonrouge.fsLib.services.RegisterProfileService
import io.kvision.state.ObservableList
import io.kvision.state.observableListOf
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

object FSLibModel {

    private val profileService = ProfileService()
    private val registerProfileService = RegisterProfileService()

    val profile: ObservableList<Profile> = observableListOf(Profile())

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
