package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.serializers.OId
import com.mongodb.client.model.UnwindOptions
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

@Suppress("unused")
abstract class IUserRoleColl<UR : IUserRole<U, UID>, U : IUser<out UID>, UID : Any, GR : IGroupRole<*, GOU>, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<UR, OId<IUserRole<U, UID>>, FILT>,
    internal val userKClass: KClass<U>,
) : Coll<ICommonContainer<UR, OId<IUserRole<U, UID>>, FILT>, UR, OId<IUserRole<U, UID>>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutine.ensureUniqueIndex(
            IUserRole<U, UID>::userId, IUserRole<U, UID>::appRoleId
        )
    }

    //    abstract val appRoleColl: Coll<out ICommonContainer<out IAppRole, OId<IAppRole>, out IApiFilter<*>>, out IAppRole, OId<IAppRole>, out IApiFilter<*>>
    abstract val appRoleColl: IAppRoleColl<out ICommonContainer<out IAppRole, OId<IAppRole>, out IApiFilter<*>>, out IAppRole, OId<IAppRole>, out IApiFilter<*>>
    abstract val groupRoleColl: IGroupRoleColl<GR, *, GOU, *>
    abstract val userGroupColl: IUserGroupColl<out IUserGroup<U, UID, *, *>, U, UID, *, *, out IApiFilter<*>>

    open fun rootUser(iUser: IUser<*>?): Boolean? = null

    suspend fun getUserPermission(
        call: ApplicationCall?,
        commonContainer: ICommonContainer<*, *, *>? = null,
        crudTask: CrudTask = CrudTask.Read,
        kCallable: KCallable<*>? = null,
        stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[2],
    ): SimpleState {
        return getUserPermission(
            user = call?.sessions?.get(klass = userKClass),
            roleType = IAppRole.RoleType.SingleAction,
            commonContainer = commonContainer,
            crudTask = crudTask,
            kCallable = kCallable,
            stackTraceElement = stackTraceElement
        )
    }

    suspend fun <U : IUser<out UID>, UID : Any> getUserPermission(
        user: U?,
        roleType: IAppRole.RoleType,
        commonContainer: ICommonContainer<*, *, *>? = null,
        crudTask: CrudTask = CrudTask.Read,
        kCallable: KCallable<*>? = null,
        stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[2],
    ): SimpleState {
        val classOwner: String
        val funcName: String
        if (kCallable != null) {
            classOwner = ((kCallable as FunctionReferenceImpl).owner as KClass<*>).simpleName ?: ""
            funcName = kCallable.name
        } else {
            classOwner = stackTraceElement.className.substringAfterLast('.')
            funcName = stackTraceElement.methodName
        }
        return getUserPermission(
            user = user,
            roleType = roleType,
            commonContainer = commonContainer,
            crudTask = crudTask,
            classOwner = classOwner,
            funcName = funcName
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getUserPermission(
        user: IUser<*>?,
        roleType: IAppRole.RoleType = IAppRole.RoleType.SingleAction,
        commonContainer: ICommonContainer<*, *, *>? = null,
        crudTask: CrudTask = CrudTask.Read,
        classOwner: String,
        funcName: String,
    ): SimpleState {
        user ?: return SimpleState(isOk = false, msgError = "Empty user")
        if (rootUser(iUser = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val (matchLabel, matchAppRole) = if (roleType == IAppRole.RoleType.CrudTask) {
            "${commonContainer?.name}" to and(
                IAppRole::roleType eq roleType,
                IAppRole::classOwner eq commonContainer?.name
            )
        } else {
            "${classOwner}::${funcName}" to and(
                IAppRole::roleType eq roleType,
                IAppRole::classOwner eq classOwner,
                IAppRole::funcName eq funcName
            )
        }
        val appRole: IAppRole = appRoleColl.coroutine.findOne(matchAppRole) ?: run {
            appRoleColl.defaultAppRoleItem(
                roleType = roleType,
                commonContainer = commonContainer,
                crudTask = crudTask,
                classOwner = classOwner,
                funcName = funcName,
            )?.let {
                appRoleColl.insertOne(item = it).item
            } ?: return SimpleState(
                isOk = false,
                msgError = "App role doesn't exist '$matchLabel' ... "
            )
        }
        val groupPermissionType: Pair<PermissionType, Set<CrudTask>>? = getGroupPermission(
            user = user,
            crudTask = crudTask,
            appRole = appRole
        )
        val userPermissionType: Pair<PermissionType, Set<CrudTask>>? = coroutine.find(
            filter = and(
                IUserRole<U, UID>::userId eq user._id,
                IUserRole<U, UID>::appRoleId eq appRole._id
            )
        ).first()?.let { it.permission to it.crudTaskSet }
        val combinedPermissionType =
            if (groupPermissionType == userPermissionType ||
                (groupPermissionType != null && userPermissionType == null)
            ) groupPermissionType else {
                userPermissionType
            }
        if (combinedPermissionType != null) {
            return if (combinedPermissionType.first == PermissionType.Allow
                || (combinedPermissionType.first == PermissionType.Default
                        && appRole.defaultPermission == PermissionType.Allow)
            ) SimpleState(isOk = crudTask in combinedPermissionType.second)
            else SimpleState(isOk = false, msgError = "Permission denied ...")
        }
        return SimpleState(isOk = false, msgError = "User not authorized ...")
    }

    private suspend fun getGroupPermission(
        user: IUser<*>,
        crudTask: CrudTask,
        appRole: IAppRole
    ): Pair<PermissionType, Set<CrudTask>>? {
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
                entry.key to entry.value.first().crudTaskSet
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
    override val crudTaskSet: Set<CrudTask> = emptySet(),
) : IGroupRole<GroupRole, GroupOfUser>
