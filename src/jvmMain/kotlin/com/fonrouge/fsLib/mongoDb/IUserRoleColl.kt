package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.serializers.OId
import com.mongodb.client.model.UnwindOptions
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IUserRoleColl<U : IUser<UID>, UID : Any, UR : IUserRole<U, UID>, GR : IGroupRole<*, GOU>, GOU : IGroupOfUser<*>, FILT : IApiFilter>(
    klass: KClass<UR>,
) : Coll<UR, OId<IUserRole<U, UID>>, FILT>(
    klass = klass
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutineColl.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }

    open fun groupRoleColl(): IGroupRoleColl<GR, *, GOU, *>? = null
    open fun userGroupColl(): IUserGroupColl<U, UID, out IUserGroup<U, UID, *, *>, *, *, out IApiFilter>? = null
    open fun rootUser(user: U?): Boolean? = null

    @Suppress("unused")
    suspend fun getUserPermission(
        user: U?,
        kCallable: KCallable<*>? = null,
    ): SimpleState {
        user ?: return SimpleState(isOk = false, "Empty user")
        if (rootUser(user = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val classOwner: String
        val funcName: String
        if (kCallable != null) {
            classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName ?: ""
            funcName = kCallable.name
        } else {
            val st = Thread.currentThread().stackTrace[2]
            classOwner = st.className.substringAfterLast('.')
            funcName = st.methodName
        }
        val appRole = AppRoleColl.coroutineColl.findOne(
            AppRole::classOwner eq classOwner,
            AppRole::funcName eq funcName
        ) ?: return SimpleState(isOk = false, msgError = "App role doesn't exist '$classOwner::$funcName' ... ")
        val groupPermissionType: PermissionType? = getGroupPermission(user, appRole)
        val userPermissionType: PermissionType? = coroutineColl.find(
            filter = and(
                IUserRole<U, UID>::userId eq user._id,
                IUserRole<U, UID>::appRoleId eq appRole._id
            )
        ).first()?.permission
        val combinedPermissionType =
            if (groupPermissionType == userPermissionType ||
                (groupPermissionType != null && userPermissionType == null)
            ) groupPermissionType else {
                userPermissionType
            }
        if (combinedPermissionType != null) {
            return if (combinedPermissionType == PermissionType.Allow
                || (combinedPermissionType == PermissionType.Default
                        && appRole.defaultPermission == PermissionType.Allow)
            ) SimpleState(isOk = true)
            else SimpleState(isOk = false, msgError = "Permission denied ...")
        }
        return SimpleState(isOk = false, msgError = "User not authorized ...")
    }

    private suspend fun getGroupPermission(user: U, appRole: AppRole): PermissionType? {
        val userGroupColl = userGroupColl() ?: return null
        val groupRoleColl = groupRoleColl() ?: return null
        val pipeline = mutableListOf<Bson>()
        pipeline.add(0, match(IUserGroup<U, UID, *, *>::userId eq user._id))
        pipeline += lookup5(
            from = groupRoleColl.collectionName,
            localField = IUserGroup<U, UID, *, *>::groupOfUserId,
            foreignField = IGroupRole<*, GOU>::groupOfUserId,
            resultField = IUserGroup<U, UID, *, *>::groupRoles,
            pipeline = listOf(
                match(IGroupRole<*, GOU>::appRoleId eq appRole._id)
            )
        )
        pipeline += IUserGroup<U, UID, *, *>::groupRoles.unwind(UnwindOptions().preserveNullAndEmptyArrays(false))
        pipeline += replaceRoot(IUserGroup<U, UID, *, *>::groupRoles)
        val groupRoleList = userGroupColl.coroutineColl.aggregate<GroupRole>(
            pipeline = pipeline
        ).toList()
        // group by permissionType
        val permissionTypeListMap = groupRoleList.groupBy { it.permission }
        return if (permissionTypeListMap.size == 1) {
            // only one permissionType
            val entry = permissionTypeListMap.entries.first()
            if (entry.value.all { it.permission == entry.key })
            // permissionType if all groupRole list has same permissionType
                entry.key
            else
                null
        } else {
            null
        }
    }
}

@Serializable
private data class GroupOfUser(
    override val _id: OId<GroupOfUser>,
    override val description: String
) : IGroupOfUser<GroupOfUser>

@Serializable
private data class GroupRole(
    override val _id: OId<GroupRole>,
    override val groupOfUserId: OId<GroupOfUser>,
    override val appRoleId: OId<AppRole>,
    override val permission: PermissionType,
) : IGroupRole<GroupRole, GroupOfUser>
