package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.serializers.OId

@Collection(name = "__userRoles")
interface IUserRole<U : IUser<UID>, UID : Any> : BaseDoc<OId<IUserRole<U, UID>>> {
    override val _id: OId<IUserRole<U, UID>>
    val userId: UID
    val appRoleId: OId<AppRole>
    val permission: PermissionType
}
