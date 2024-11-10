package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IAppRole
import com.fonrouge.fsLib.model.state.ItemState

abstract class IAppRoleColl<CC : ICommonContainer<T, ID, FILT>, T : IAppRole<ID>, ID : Any, FILT : IApiFilter<*>>(
    commonContainer: CC
) : Coll<CC, T, ID, FILT>(
    commonContainer = commonContainer
) {
    open suspend fun insertDefaultAppRole(
        roleType: IAppRole.RoleType,
        commonContainer: ICommonContainer<*, *, *>?,
        crudTask: CrudTask,
        classOwner: String,
        funcName: String
    ): ItemState<T> = ItemState(isOk = false)
}
