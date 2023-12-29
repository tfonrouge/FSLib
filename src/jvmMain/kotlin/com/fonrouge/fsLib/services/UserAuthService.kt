package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.mongoDb.AppRoleDb
import com.fonrouge.fsLib.mongoDb.IUserRoleColl
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.kvision.remote.ServiceException
import org.litote.kmongo.eq
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

inline fun <reified U : IUser<*>> ApplicationCall.getUser(): U? {
    return sessions.get()
}

@Suppress("unused")
inline fun <reified U : IUser<*>> ApplicationCall.setUser(user: U) {
    sessions.set(user)
}

@Suppress("unused")
inline fun <RESP, reified U : IUser<*>> ApplicationCall.withUser(block: (U) -> RESP): RESP {
    return getUser<U>()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}

@Suppress("unused")
suspend fun <U : IUser<UID>, UID : Any, UR : IUserRole<U, UID>> getUserPermission(
    user: U?,
    kCallable: KCallable<*>,
    userRoleColl: IUserRoleColl<U, UID, UR, *>
): SimpleState {
    user ?: return SimpleState(isOk = false, "Empty user")
    if (user.rootUser) return SimpleState(isOk = true)
    val classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName
    val funcName = kCallable.name
    val appRole = AppRoleDb.coroutineColl.findOne(
        AppRole::classOwner eq classOwner,
        AppRole::funcName eq funcName
    ) ?: return SimpleState(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
    userRoleColl.coroutineColl.find(
        filter = IUserRole<U, UID>::userId eq user._id
    ).toList().forEach { userRole ->
        if (userRole.appRoleId == appRole._id)
            return if (userRole.permission == PermissionType.Allow
                || (userRole.permission == PermissionType.Default
                        && appRole.defaultPermission == PermissionType.Allow)
            ) SimpleState(isOk = true)
            else SimpleState(isOk = false, msgError = "Permission denied ...")
    }
    return SimpleState(isOk = false, msgError = "User not authorized ...")
}
