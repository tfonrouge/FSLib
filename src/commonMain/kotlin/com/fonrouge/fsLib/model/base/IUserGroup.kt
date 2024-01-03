package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IUserGroup<U : IUser<UID>, UID : Any> : BaseDoc<OId<IUserGroup<U, UID>>> {
    override val _id: OId<IUserGroup<U, UID>>
    val userId: UID
    val groupOfUserId: OId<IGroupOfUser>
    var groupRoles: List<IGroupRole>
}
