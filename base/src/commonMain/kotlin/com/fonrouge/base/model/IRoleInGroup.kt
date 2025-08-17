package com.fonrouge.base.model

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.types.OId

/**
 * Interface representing a role within a group.
 *
 * @param T The type parameter for the identifier.
 * @param GOU The type of group of user.
 */
interface IRoleInGroup<T : Any, GOU : IGroupOfUser<*>> : BaseDoc<OId<T>> {
    override val _id: OId<T>
    val groupOfUserId: OId<GOU>
    val appRoleId: OId<out IAppRole<*>>
    val permission: PermissionType
    val crudTaskSet: Set<CrudTask>?
}
