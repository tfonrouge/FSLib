package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.serializers.OId
import com.mongodb.client.model.UnwindOptions
import kotlinx.serialization.Serializable
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.match
import org.litote.kmongo.replaceRoot
import org.litote.kmongo.unwind
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IUserRoleColl<U : IUser<UID>, UID : Any, UR : IUserRole<U, UID>, FILT : IApiFilter>(
    klass: KClass<UR>,
) : Coll<UR, OId<IUserRole<U, UID>>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutineColl.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }

    open fun groupRoleColl(): IGroupRoleColl<out IGroupRole, out IApiFilter>? = null

    //    open fun groupUserColl(): IGroupUserColl<out IGroupUser, out IApiFilter>? = null
    open fun userGroupColl(): IUserGroupColl<U, UID, out IUserGroup<U, UID>, out IApiFilter>? = null
    open fun rootUser(user: U?): Boolean? = null

    @Suppress("unused")
    suspend fun getUserPermission(
        user: U?,
        kCallable: KCallable<*>,
    ): SimpleState {
        user ?: return SimpleState(isOk = false, "Empty user")
        if (rootUser(user = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName
        val funcName = kCallable.name
        val appRole = AppRoleColl.coroutineColl.findOne(
            AppRole::classOwner eq classOwner,
            AppRole::funcName eq funcName
        ) ?: return SimpleState(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
//        val allowed = checkGroupUserPermission(user, appRole)
        coroutineColl.find(
            filter = IUserRole<U, UID>::userId eq user._id
        ).toList().forEach { userRole: UR ->
            if (userRole.appRoleId == appRole._id)
                return if (userRole.permission == PermissionType.Allow
                    || (userRole.permission == PermissionType.Default
                            && appRole.defaultPermission == PermissionType.Allow)
                ) SimpleState(isOk = true)
                else SimpleState(isOk = false, msgError = "Permission denied ...")
        }
        return SimpleState(isOk = false, msgError = "User not authorized ...")
    }

    private suspend fun checkGroupUserPermission(user: U, appRole: AppRole): Boolean? {
        val userGroupColl = userGroupColl() ?: return null
        val groupRoleColl = groupRoleColl() ?: return null
        val pipeline = mutableListOf<Bson>()
        pipeline.add(0, match(IUserGroup<U, UID>::userId eq user._id))
        pipeline += lookup5(
            from = groupRoleColl.collectionName,
            localField = IUserGroup<U, UID>::groupUserId,
            foreignField = IGroupRole::groupUserId,
            resultField = IUserGroup<U, UID>::groupRoles,
            pipeline = listOf(
                match(IGroupRole::appRoleId eq appRole._id)
            )
        )
        pipeline += IUserGroup<U, UID>::groupRoles.unwind(UnwindOptions().preserveNullAndEmptyArrays(false))
        pipeline += replaceRoot(IUserGroup<U, UID>::groupRoles)
        val r = userGroupColl.coroutineColl.aggregate<Document>(
            pipeline = pipeline
        )
        println(r)
        return null
    }
}

@Serializable
data class RolesInGroupsByUser(
    val groupUserId: OId<IGroupUser>,
    val groupRoles: List<IGroupRole>
)
