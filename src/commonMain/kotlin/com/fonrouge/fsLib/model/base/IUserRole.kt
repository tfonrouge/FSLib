package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.serializers.OId

@Collection(name = "__userRoles")
interface IUserRole<U : IUser<UID>, UID : Any> : BaseDoc<OId<IUserRole<U, UID>>> {
    override var _id: OId<IUserRole<U, UID>>
    var userId: UID
    var appRoleId: OId<AppRole>
    var permission: PermissionType
}
