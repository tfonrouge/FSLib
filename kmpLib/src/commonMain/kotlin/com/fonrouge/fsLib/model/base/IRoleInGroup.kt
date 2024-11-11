package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.serializers.OId

interface IRoleInGroup<T : Any, GOU : IGroupOfUser<*>> : BaseDoc<OId<T>> {
    override val _id: OId<T>
    val groupOfUserId: OId<GOU>
    val appRoleId: OId<out IAppRole<*>>
    val permission: PermissionType
    val crudTaskSet: Set<CrudTask>
}
