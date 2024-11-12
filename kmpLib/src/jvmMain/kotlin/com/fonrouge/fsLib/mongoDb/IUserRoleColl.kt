package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.*
import com.fonrouge.fsLib.model.base.IAppRole.BaseRolePermission
import com.fonrouge.fsLib.model.base.IAppRole.RoleType
import com.fonrouge.fsLib.model.state.ItemState
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
    abstract val roleInGroupColl: IRoleInGroupColl<GR, *, GOU, *>
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
        val (matchLabel, matchAppRole) = "${classOwner}::${funcName}" to and(
            IAppRole<*>::roleType eq RoleType.SingleAction,
            IAppRole<*>::classOwner eq classOwner,
            IAppRole<*>::funcName eq funcName
        )
        return permissionState(
            roleType = RoleType.SingleAction,
            user = user,
            crudTask = null
        ) {
            appRoleColl.coroutine.findOne(matchAppRole)?.let {
                ItemState(item = it)
            } ?: appRoleColl.insertSingleActionRole(
                classOwner = classOwner,
                funcName = funcName
            ).item?.let {
                ItemState(item = it)
            } ?: ItemState(
                isOk = false,
                msgError = "App role doesn't exist '$matchLabel' ... "
            )
        }
    }

    suspend fun permissionState(
        roleType: RoleType,
        user: IUser<*>,
        crudTask: CrudTask? = null,
        insertBlock: suspend () -> ItemState<out IAppRole<*>>,
    ): SimpleState {
        if (rootUser(iUser = user) == true) return SimpleState(isOk = true, msgOk = "as rootUser")
        val appRole: IAppRole<*> = insertBlock().let { itemState ->
            itemState.item ?: return SimpleState(
                isOk = false,
                msgError = itemState.msgError ?: "App role doesn't exist"
            )
        }
        coroutine.find(
            filter = and(
                IUserRole<U, UID>::userId eq user._id,
                IUserRole<U, UID>::appRoleId eq appRole._id
            )
        ).first()?.let { it: UR ->
            return buildSimpleState(
                baseRolePermission = if (crudTask in it.crudTaskSet) {
                    when (it.permission) {
                        PermissionType.Allow -> BaseRolePermission.Allow
                        PermissionType.Deny -> BaseRolePermission.Deny
                        PermissionType.Default -> when (appRole.defaultPermission) {
                            BaseRolePermission.Allow -> BaseRolePermission.Allow
                            BaseRolePermission.Deny -> BaseRolePermission.Deny
                        }
                    }
                } else BaseRolePermission.Deny
            )
        }
        return buildSimpleState(
            baseRolePermission = getGroupPermission(
                user = user,
                appRole = appRole,
                crudTask = crudTask
            )
        )
    }

    private fun buildSimpleState(baseRolePermission: BaseRolePermission): SimpleState {
        val granted = baseRolePermission == BaseRolePermission.Allow
        return SimpleState(
            isOk = granted,
            msgOk = if (granted) "Permission granted" else null,
            msgError = if (granted.not()) "Permission denied" else null
        )
    }

    private fun buildDefaultAppRolePermission(
        appRole: IAppRole<*>,
        crudTask: CrudTask? = null,
    ): BaseRolePermission {
        return when (appRole.roleType) {
            RoleType.SingleAction -> appRole.defaultPermission
            RoleType.CrudTask -> (crudTask in appRole.defaultCrudTaskSet).let { crudTaskContained ->
                when (appRole.defaultPermission) {
                    BaseRolePermission.Allow -> if (crudTaskContained) BaseRolePermission.Allow else BaseRolePermission.Deny
                    BaseRolePermission.Deny -> if (crudTaskContained) BaseRolePermission.Deny else BaseRolePermission.Allow
                }
            }
        }
    }

    private suspend fun getGroupPermission(
        user: IUser<*>,
        appRole: IAppRole<out Any>,
        crudTask: CrudTask? = null,
    ): BaseRolePermission {
        val userGroupColl = userGroupColl
        val roleInGroupColl = this@IUserRoleColl.roleInGroupColl
        val pipeline = mutableListOf<Bson>()
        pipeline.add(0, match(IUserGroup<U, UID, *, *>::userId eq user._id))
        pipeline += lookup5(
            from = roleInGroupColl.commonContainer.itemKClass.collectionName,
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
        val permissionTypes = groupRoleList.filter { roleInGroup ->
            crudTask?.let { it in roleInGroup.crudTaskSet } != false
        }
        if (permissionTypes.isEmpty()) return buildDefaultAppRolePermission(appRole, crudTask)
        if (permissionTypes.size == 1) return when (permissionTypes.first().permission) {
            PermissionType.Allow -> BaseRolePermission.Allow
            PermissionType.Deny -> BaseRolePermission.Deny
            PermissionType.Default -> buildDefaultAppRolePermission(appRole, crudTask)
        }
        if (appRole.upVoteInGroup == BaseRolePermission.Allow && permissionTypes.any { it.permission == PermissionType.Allow }) {
            return BaseRolePermission.Allow
        }
        if (appRole.upVoteInGroup == BaseRolePermission.Deny && permissionTypes.any { it.permission == PermissionType.Deny }) {
            return BaseRolePermission.Deny
        }
        return buildDefaultAppRolePermission(appRole, crudTask)
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
