package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IGroupRole : BaseDoc<OId<IGroupRole>> {
    override val _id: OId<IGroupRole>
    val groupUserId: OId<IGroupUser>
    val appRoleId: OId<AppRole>
    val permission: PermissionType
}
