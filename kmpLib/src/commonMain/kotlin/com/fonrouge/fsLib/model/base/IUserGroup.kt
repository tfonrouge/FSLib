package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId

interface IUserGroup<U : IUser<out UID>, UID : Any, GOU : IGroupOfUser<*>, GR : IRoleInGroup<*, GOU>> :
    BaseDoc<OId<IUserGroup<U, UID, GOU, GR>>> {
    override val _id: OId<IUserGroup<U, UID, GOU, GR>>
    val userId: UID
    val groupOfUserId: OId<GOU>
    var groupRoles: List<GR>
}
