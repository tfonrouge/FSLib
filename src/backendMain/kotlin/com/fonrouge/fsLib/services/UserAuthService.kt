package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.SimpleResponse
import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.base.PermissionType
import com.fonrouge.fsLib.model.base.SysUserRole
import com.fonrouge.fsLib.mongoDb.AppRoleDb
import com.fonrouge.fsLib.mongoDb.SysUserRoleDb
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.kvision.remote.ServiceException
import org.litote.kmongo.eq
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

inline fun <reified T : ISysUser> ApplicationCall.getSysUser(): T? {
    return sessions.get()
}

@Suppress("unused")
inline fun <reified T : ISysUser> ApplicationCall.setSysUser(sysUser: T) {
    sessions.set(sysUser)
}

@Suppress("unused")
inline fun <RESP, reified U : ISysUser> ApplicationCall.withSysUser(block: (U) -> RESP): RESP {
    return getSysUser<U>()?.let {
        block(it)
    } ?: throw ServiceException("App User not set!")
}

@Suppress("unused")
suspend inline fun <reified U : ISysUser> ApplicationCall?.getUserPermission(kCallable: KCallable<*>): SimpleResponse {
    this ?: return SimpleResponse(isOk = false, msgError = "Operation denied ...")
    val user = getSysUser<U>() ?: return SimpleResponse(isOk = false, msgError = "User not valid ...")
    if (user.rootUser) {
        return SimpleResponse(isOk = true)
    }
    val classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName
    val funcName = kCallable.name
    val appRole = AppRoleDb.coroutineColl.findOne(
        AppRole::classOwner eq classOwner,
        AppRole::funcName eq funcName
    ) ?: return SimpleResponse(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
    SysUserRoleDb.coroutineColl.find(
        filter = SysUserRole::sysUser_id eq user._id
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
