package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.serializers.OId

interface IUserRole<U : IUser<UID>, UID : Any> : BaseDoc<OId<IUserRole<U, UID>>> {
    override val _id: OId<IUserRole<U, UID>>
    val userId: UID
    val appRoleId: OId<IAppRole>
    val permission: PermissionType
    val crudTaskSet: Set<CrudTask>
}
