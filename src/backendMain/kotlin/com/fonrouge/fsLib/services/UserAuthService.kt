package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.SimpleResponse
import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.AppUser
import com.fonrouge.fsLib.model.base.PermissionType
import com.fonrouge.fsLib.model.base.UserRole
import com.fonrouge.fsLib.mongoDb.AppRoleDb
import com.fonrouge.fsLib.mongoDb.UserRoleDb
import com.google.inject.Inject
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.litote.kmongo.eq
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

class UserAuthService {
    @Inject
    lateinit var call: ApplicationCall
}

suspend fun ApplicationCall.getUserPermission(kCallable: KCallable<*>): SimpleResponse {
    val user = sessions.get<AppUser>() ?: return SimpleResponse(isOk = false, msgError = "User not valid ...")
    if (user.rootUser) {
        return SimpleResponse(isOk = true)
    }
    val classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName
    val funcName = kCallable.name
    val appRole = AppRoleDb.coroutineColl.findOne(
        AppRole::classOwner eq classOwner,
        AppRole::funcName eq funcName
    ) ?: return SimpleResponse(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
    UserRoleDb.coroutineColl.find(
        filter = UserRole::user_id eq user._id
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
