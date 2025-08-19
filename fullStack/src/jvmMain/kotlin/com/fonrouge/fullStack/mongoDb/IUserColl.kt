package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IUser
import com.fonrouge.base.model.UserSession
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

abstract class IUserColl<CCU : ICommonContainer<U, UID, FILT>, U : IUser<UID>, UID : Any, FILT : IApiFilter<*>>(
    commonContainer: CCU,
    debug: Boolean = false
) : Coll<CCU, U, UID, FILT>(
    commonContainer = commonContainer,
    debug = debug,
) {
    suspend fun userFromCall(call: ApplicationCall?) : U? {
        val userSession: UserSession<UID>? = call?.sessions?.get()
        return findById(userSession?.userId)
    }
}
