package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.SimpleResponse
import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.AppUser
import com.fonrouge.fsLib.model.base.AppUserRole
import com.fonrouge.fsLib.model.base.PermissionType
import com.fonrouge.fsLib.mongoDb.AppRoleDb
import com.fonrouge.fsLib.mongoDb.AppUserRoleDb
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.kvision.remote.ServiceException
import org.litote.kmongo.eq
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

@Suppress("unused")
fun CurrentSession.getAppUser(): AppUser? {
    return get()
}

@Suppress("unused")
suspend fun <RESP> ApplicationCall.withAppUser(block: suspend (AppUser) -> RESP): RESP {
    return sessions.getAppUser()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}

@Suppress("unused")
suspend fun ApplicationCall?.getUserPermission(kCallable: KCallable<*>): SimpleResponse {
    this ?: return SimpleResponse(isOk = false, msgError = "Operation denied ...")
    val user = sessions.getAppUser() ?: return SimpleResponse(isOk = false, msgError = "User not valid ...")
    if (user.rootUser) {
        return SimpleResponse(isOk = true)
    }
    val classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName
    val funcName = kCallable.name
    val appRole = AppRoleDb.coroutineColl.findOne(
        AppRole::classOwner eq classOwner,
        AppRole::funcName eq funcName
    ) ?: return SimpleResponse(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
    AppUserRoleDb.coroutineColl.find(
        filter = AppUserRole::appUser_id eq user._id
    ).toList().forEach { userRole ->
        if (userRole.appRole_id == appRole._id)
            return if (userRole.permission == PermissionType.Allow
                || (userRole.permission == PermissionType.Default
                        && appRole.defaultPermission == PermissionType.Allow)
            ) SimpleResponse(isOk = true)
            else SimpleResponse(isOk = false, msgError = "Permission denied ...")
    }
    return SimpleResponse(isOk = false, msgError = "User not authorized ...")
}
