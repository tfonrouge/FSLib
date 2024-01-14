package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IGroupRole<T : Any, GOU : IGroupOfUser<*>> : BaseDoc<OId<T>> {
    override val _id: OId<T>
    val groupOfUserId: OId<GOU>
    val appRoleId: OId<IAppRole>
    val permission: PermissionType
}
