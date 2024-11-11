package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.base.IAppRole.RoleType
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
abstract class IUserRoleColl<UR : IUserRole<U, UID>, U : IUser<out UID>, UID : Any, GR : IRoleInGroup<*, GOU>, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
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
    abstract val appRoleColl: IAppRoleColl<*, *, *, *>
    abstract val groupRoleColl: IGroupRoleColl<GR, *, GOU, *>
    abstract val userGroupColl: IUserGroupColl<out IUserGroup<U, UID, *, *>, U, UID, *, *, out IApiFilter<*>>

    open fun rootUser(iUser: IUser<*>?): Boolean? = null

    suspend fun getSingleActionPermission(
        call: ApplicationCall,
        kCallable: KCallable<*>? = null,
        stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[2],
    ): SimpleState {
        return getSingleActionPermission(
            user = call.sessions.get(klass = userKClass),
            kCallable = kCallable,
            stackTraceElement = stackTraceElement
        )
    }

    suspend fun <U : IUser<out UID>, UID : Any> getSingleActionPermission(
        user: U?,
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
        return getSingleActionPermission(
            user = user,
            classOwner = classOwner,
            funcName = funcName
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getSingleActionPermission(
        user: IUser<*>?,
        classOwner: String,
        funcName: String,
    ): SimpleState {
        user ?: return SimpleState(isOk = false, msgError = "Empty user")
        if (rootUser(iUser = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val (matchLabel, matchAppRole) = "${classOwner}::${funcName}" to and(
            IAppRole<*>::roleType eq RoleType.SingleAction,
            IAppRole<*>::classOwner eq classOwner,
            IAppRole<*>::funcName eq funcName
        )

        val appRole: IAppRole<out Any> = appRoleColl.coroutine.findOne(matchAppRole) ?: run {
            println(matchAppRole.json)
            val itemState = appRoleColl.insertSingleActionRole(
                classOwner = classOwner,
                funcName = funcName
            )

            itemState.item ?: return SimpleState(
                isOk = false,
                msgError = "App role doesn't exist '$matchLabel' ... "
            )
        }
        if (appRole.defaultPermission == PermissionType.Allow) {
            return SimpleState(isOk = true)
        }
        val groupPermission: Pair<PermissionType, Any>? = getGroupPermission(
            user = user,
            appRole = appRole
        )
        val userPermission: Pair<PermissionType, Any>? = coroutine.find(
            filter = and(
                IUserRole<U, UID>::userId eq user._id,
                IUserRole<U, UID>::appRoleId eq appRole._id
            )
        ).first()?.let { it.permission to it.crudTaskSet }
        val combinedPermissionType =
            if (groupPermission == userPermission || (groupPermission != null && userPermission == null)) {
                groupPermission
            } else {
                userPermission
            }
        if (combinedPermissionType != null) {
            return if (combinedPermissionType.first == PermissionType.Allow
                || (combinedPermissionType.first == PermissionType.Default
                        && appRole.defaultPermission == PermissionType.Allow)
            ) SimpleState(isOk = true)
            else SimpleState(isOk = false, msgError = "Permission denied ...")
        }
        return SimpleState(isOk = false, msgError = "User not authorized ...")
    }

    suspend fun getGroupPermission(
        user: IUser<*>,
        appRole: IAppRole<out Any>
    ): Pair<PermissionType, Set<CrudTask>>? {
        val userGroupColl = userGroupColl
        val groupRoleColl = groupRoleColl
        val pipeline = mutableListOf<Bson>()
        pipeline.add(0, match(IUserGroup<U, UID, *, *>::userId eq user._id))
        pipeline += lookup5(
            from = groupRoleColl.commonContainer.itemKClass.collectionName,
            localField = IUserGroup<U, UID, *, *>::groupOfUserId,
            foreignField = IRoleInGroup<*, GOU>::groupOfUserId,
            resultField = IUserGroup<U, UID, *, *>::roleInGroups,
            pipeline = listOf(
                match(IRoleInGroup<*, GOU>::appRoleId eq appRole._id)
            )
        )
        pipeline += IUserGroup<U, UID, *, *>::roleInGroups.unwind(
            UnwindOptions().preserveNullAndEmptyArrays(
                false
            )
        )
        pipeline += replaceRoot(IUserGroup<U, UID, *, *>::roleInGroups)
        val groupRoleList = userGroupColl.coroutine.aggregate<RoleInGroup>(
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
private data class RoleInGroup(
    override val _id: OId<RoleInGroup>,
    override val groupOfUserId: OId<GroupOfUser>,
    override val appRoleId: OId<out IAppRole<*>>,
    override val permission: PermissionType,
    override val crudTaskSet: Set<CrudTask> = emptySet(),
) : IRoleInGroup<RoleInGroup, GroupOfUser>
