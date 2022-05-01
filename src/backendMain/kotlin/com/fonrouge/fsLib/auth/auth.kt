package com.fonrouge.fsLib.auth

import com.fonrouge.fsLib.dataModel.UserProfile
import com.fonrouge.fsLib.dataModel.userProfileColl
import com.fonrouge.fsLib.mongoDb.collation
import com.fonrouge.fsLib.services.Profile
import com.fonrouge.fsLib.services.ProfileServiceManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.kvision.remote.applyRoutes
import org.apache.commons.codec.digest.DigestUtils
import org.litote.kmongo.eq

@Suppress("unused")
fun Route.applyLogin() {
    post("login") {
        val result = call.principal<UserIdPrincipal>()?.let { userIdPrincipal ->
            userProfileColl?.find(UserProfile::userName eq userIdPrincipal.name)?.collation(collation)?.first()
                ?.let { userProfile ->
                    val profile = Profile(
                        id = userProfile._id.toHexString(),
                        name = userProfile.name,
                        username = userProfile.userName,
                        password = null,
                        password2 = null
                    )
                    call.sessions.set(profile)
                    HttpStatusCode.OK
                }
        }
        if (result == null) {
            call.sessions.clear<Profile>()
        }
        call.respond(result ?: HttpStatusCode.Unauthorized)
    }
    applyRoutes(ProfileServiceManager)
}
