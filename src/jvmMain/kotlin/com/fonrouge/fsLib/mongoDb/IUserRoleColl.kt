package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
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
abstract class IUserRoleColl<UR : IUserRole<U, UID>, U : IUser<UID>, UID : Any, GR : IGroupRole<*, GOU>, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<UR, OId<IUserRole<U, UID>>, FILT>
) : Coll<ICommonContainer<UR, OId<IUserRole<U, UID>>, FILT>, UR, OId<IUserRole<U, UID>>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutine.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }

    abstract val appRoleColl: Coll<out ICommonContainer<out IAppRole, OId<IAppRole>, out IApiFilter<*>>, out IAppRole, OId<IAppRole>, out IApiFilter<*>>
    abstract val groupRoleColl: IGroupRoleColl<GR, *, GOU, *>
    abstract val userGroupColl: IUserGroupColl<out IUserGroup<U, UID, *, *>, U, UID, *, *, out IApiFilter<*>>
    open fun rootUser(user: U?): Boolean? = null

    suspend fun getUserPermission(
        user: U?,
        kCallable: KCallable<*>? = null,
    ): SimpleState {
        val classOwner: String
        val funcName: String
        if (kCallable != null) {
            classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName ?: ""
            funcName = kCallable.name
        } else {
            val st = Thread.currentThread().stackTrace[3]
            classOwner = st.className.substringAfterLast('.')
            funcName = st.methodName
        }
        return getUserPermission(user = user, classOwner = classOwner, funcName = funcName)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getUserPermission(
        user: U?,
        classOwner: String,
        funcName: String,
    ): SimpleState {
        user ?: return SimpleState(isOk = false, msgError = "Empty user")
        if (rootUser(user = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val appRole = appRoleColl.coroutine.findOne(
            IAppRole::classOwner eq classOwner,
            IAppRole::funcName eq funcName
        ) ?: return SimpleState(
            isOk = false,
            msgError = "App role doesn't exist '$classOwner::$funcName' ... "
        )
        val groupPermissionType: PermissionType? = getGroupPermission(user, appRole)
        val userPermissionType: PermissionType? = coroutine.find(
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

    private suspend fun getGroupPermission(user: U, appRole: IAppRole): PermissionType? {
        val userGroupColl = userGroupColl
        val groupRoleColl = groupRoleColl
        val pipeline = mutableListOf<Bson>()
        pipeline.add(0, match(IUserGroup<U, UID, *, *>::userId eq user._id))
        pipeline += lookup5(
            from = groupRoleColl.commonContainer.itemKClass.collectionName,
            localField = IUserGroup<U, UID, *, *>::groupOfUserId,
            foreignField = IGroupRole<*, GOU>::groupOfUserId,
            resultField = IUserGroup<U, UID, *, *>::groupRoles,
            pipeline = listOf(
                match(IGroupRole<*, GOU>::appRoleId eq appRole._id)
            )
        )
        pipeline += IUserGroup<U, UID, *, *>::groupRoles.unwind(
            UnwindOptions().preserveNullAndEmptyArrays(
                false
            )
        )
        pipeline += replaceRoot(IUserGroup<U, UID, *, *>::groupRoles)
        val groupRoleList = userGroupColl.coroutine.aggregate<GroupRole>(
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
    override val appRoleId: OId<IAppRole>,
    override val permission: PermissionType,
) : IGroupRole<GroupRole, GroupOfUser>
