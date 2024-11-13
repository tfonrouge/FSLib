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
abstract class IRoleInUserColl<UR : IRoleInUser<U, UID>, U : IUser<out UID>, UID : Any, GR : IRoleInGroup<*, GOU>, GOU : IGroupOfUser<*>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<UR, OId<IRoleInUser<U, UID>>, FILT>,
    internal val userKClass: KClass<U>,
) : Coll<ICommonContainer<UR, OId<IRoleInUser<U, UID>>, FILT>, UR, OId<IRoleInUser<U, UID>>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<UR>.ensureIndexes() {
        coroutine.ensureUniqueIndex(
            IRoleInUser<U, UID>::userId, IRoleInUser<U, UID>::appRoleId
        )
    }

    //    abstract val appRoleColl: Coll<out ICommonContainer<out IAppRole, OId<IAppRole>, out IApiFilter<*>>, out IAppRole, OId<IAppRole>, out IApiFilter<*>>
    abstract val appRoleColl: IAppRoleColl<*, *, *, *>
    abstract val roleInGroupColl: IRoleInGroupColl<GR, *, GOU, *>
    abstract val userGroupColl: IUserGroupColl<out IUserGroup<U, UID, *, *>, U, UID, *, *, out IApiFilter<*>>

    /**
     * Determines whether the given user has root privileges.
     *
     * @param iUser The user to check for root privileges.
     * @return A Boolean indicating whether the user has root privileges, or null if the check could not be performed.
     */
    open fun rootUser(iUser: IUser<*>?): Boolean? = null

    /**
     * Retrieves the single action permission for the current user session.
     *
     * @param call The current application call containing the user session.
     * @param kCallable An optional callable reference for logging or permission purposes.
     * @param stackTraceElement The stack trace element for logging or debugging purposes.
     * @return A pair containing the user (if valid) and the permission state.
     */
    suspend fun getSingleActionPermission(
        call: ApplicationCall,
        kCallable: KCallable<*>? = null,
        stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[2],
    ): Pair<U?, SimpleState> {
        val user = call.sessions.get(klass = userKClass) ?: return null to SimpleState(
            isOk = false,
            msgError = "User not valid"
        )
        return user to getSingleActionPermission(
            user = user,
            kCallable = kCallable,
            stackTraceElement = stackTraceElement
        )
    }

    /**
     * Retrieves the single action permission for a user based on a callable reference or stack trace element.
     *
     * @param user The user for whom the permission is being validated.
     * @param kCallable An optional callable reference for the function whose permission is being checked. Defaults to null.
     * @param stackTraceElement The stack trace element from which the calling method's information is derived. Defaults to the caller's context.
     * @return A SimpleState object representing the permission state for the user.
     */
    suspend fun getSingleActionPermission(
        user: U,
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

    /**
     * Retrieves the permission state for a specified user regarding a specific action.
     *
     * @param user The user for whom the permission is being checked.
     * @param classOwner The name of the class owning the function whose permission is being checked.
     * @param funcName The name of the function whose permission is being checked.
     * @return A SimpleState object representing the permission state for the user.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getSingleActionPermission(
        user: U,
        classOwner: String,
        funcName: String,
    ): SimpleState {
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

    /**
     * Retrieves the permission state for a specified user based on their role and a potential CRUD task.
     *
     * @param roleType The type of role being checked.
     * @param user The user for whom the permission is being checked.
     * @param crudTask An optional CRUD task that may further specify the permission being checked. Defaults to null.
     * @param insertBlock A suspending block that provides the state of an application role.
     * @return A SimpleState object representing the permission state for the user.
     */
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
                IRoleInUser<U, UID>::userId eq user._id,
                IRoleInUser<U, UID>::appRoleId eq appRole._id
            )
        ).first()?.let { it: UR ->
            return buildSimpleState(
                baseRolePermission = if (it.crudTaskSet?.contains(crudTask) == true) {
                    when (it.permission) {
                        PermissionType.Allow -> BaseRolePermission.Allow
                        PermissionType.Deny -> BaseRolePermission.Deny
                        PermissionType.Default -> when (appRole.defaultPermission) {
                            BaseRolePermission.Allow -> BaseRolePermission.Allow
                            BaseRolePermission.Deny -> BaseRolePermission.Deny
                        }
                    }
                } else BaseRolePermission.Deny,
                appRole = appRole,
                crudTask = crudTask
            )
        }
        return buildSimpleState(
            baseRolePermission = getGroupPermission(
                user = user,
                appRole = appRole,
                crudTask = crudTask
            ),
            appRole = appRole,
            crudTask = crudTask
        )
    }

    /**
     * Builds a SimpleState object based on role permissions and an optional CRUD task.
     *
     * @param baseRolePermission The base role permission indicating whether the action is allowed or denied.
     * @param appRole The application role associated with the user.
     * @param crudTask An optional CRUD task specifying the type of CRUD operation. Default is null.
     * @return A SimpleState object representing whether the permission is granted or denied, along with an appropriate message.
     */
    private fun buildSimpleState(
        baseRolePermission: BaseRolePermission,
        appRole: IAppRole<*>,
        crudTask: CrudTask?
    ): SimpleState {
        val granted = baseRolePermission == BaseRolePermission.Allow
        val preLabel = "${appRole.roleType} ${crudTask?.let { "[" + it.name + "]" } ?: ""} ${appRole.description}"
        return SimpleState(
            isOk = granted,
            msgOk = if (granted) "$preLabel: Permission granted" else null,
            msgError = if (granted.not()) "$preLabel: Permission denied" else null
        )
    }

    /**
     * Builds the default application role permission based on the role type and optional CRUD task.
     *
     * @param appRole The application role for which the permission is being built.
     * @param crudTask An optional CRUD task specifying the type of CRUD operation. Default is null.
     * @return The default base role permission, either Allow or Deny, depending on the role type and CRUD task.
     */
    private fun buildDefaultAppRolePermission(
        appRole: IAppRole<*>,
        crudTask: CrudTask? = null,
    ): BaseRolePermission {
        return when (appRole.roleType) {
            RoleType.SingleAction -> appRole.defaultPermission
            RoleType.CrudTask -> (appRole.defaultCrudTaskSet?.contains(crudTask) == true).let { crudTaskContained ->
                when (appRole.defaultPermission) {
                    BaseRolePermission.Allow -> if (crudTaskContained) BaseRolePermission.Allow else BaseRolePermission.Deny
                    BaseRolePermission.Deny -> if (crudTaskContained) BaseRolePermission.Deny else BaseRolePermission.Allow
                }
            }
        }
    }

    /**
     * Retrieves the group-level permission for a specified user and application role.
     *
     * @param user The user whose group permissions are being checked.
     * @param appRole The application role for which the group's permission is being determined.
     * @param crudTask An optional CRUD task that further specifies the permission being checked. Defaults to null.
     * @return The base role permission (Allow, Deny, or Default) for the user's group with respect to the specified application role and CRUD task.
     */
    private suspend fun getGroupPermission(
        user: IUser<*>,
        appRole: IAppRole<out Any>,
        crudTask: CrudTask? = null,
    ): BaseRolePermission {
        val userGroupColl = userGroupColl
        val roleInGroupColl = this@IRoleInUserColl.roleInGroupColl
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
            crudTask?.let { roleInGroup.crudTaskSet?.contains(it) == true } != false
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
    override val crudTaskSet: Set<CrudTask>?,
) : IRoleInGroup<RoleInGroup, GroupOfUser>
