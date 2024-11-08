package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.base.IAppRole
import com.fonrouge.fsLib.serializers.OId

abstract class IAppRoleColl<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : OId<T>, FILT : IApiFilter<*>>(
    commonContainer: ICommonContainer<IAppRole, OId<IAppRole>, IApiFilter<*>>
) : Coll<ICommonContainer<IAppRole, OId<IAppRole>, IApiFilter<*>>, IAppRole, OId<IAppRole>, IApiFilter<*>>(
    commonContainer = commonContainer
) {
    open fun defaultAppRoleItem(
        roleType: IAppRole.RoleType,
        commonContainer: ICommonContainer<*, *, *>?,
        crudTask: CrudTask,
        classOwner: String,
        funcName: String,
    ): IAppRole? = null
}
