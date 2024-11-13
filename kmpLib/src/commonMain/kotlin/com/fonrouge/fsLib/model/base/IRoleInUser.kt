package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.serializers.OId

interface IRoleInUser<U : IUser<out UID>, UID : Any> : BaseDoc<OId<IRoleInUser<U, UID>>> {
    override val _id: OId<IRoleInUser<U, UID>>
    val userId: UID
    val appRoleId: OId<out IAppRole<*>>
    val permission: PermissionType
    val crudTaskSet: Set<CrudTask>?
}
