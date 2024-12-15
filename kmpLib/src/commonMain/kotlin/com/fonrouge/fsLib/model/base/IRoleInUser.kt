package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.types.OId

/**
 * Interface representing a role associated with a user in the application.
 *
 * @param U The type of the user implementing `IUser`.
 * @param UID The type of the user identifier.
 */
interface IRoleInUser<U : IUser<out UID>, UID : Any> : BaseDoc<OId<IRoleInUser<U, UID>>> {
    override val _id: OId<IRoleInUser<U, UID>>
    val userId: UID
    val appRoleId: OId<out IAppRole<*>>
    val permission: PermissionType
    val crudTaskSet: Set<CrudTask>?
}
