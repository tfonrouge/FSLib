package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.IAppRole
import com.fonrouge.fsLib.model.base.IAppRole.RoleType
import com.fonrouge.fsLib.model.state.ItemState
import com.mongodb.client.model.IndexOptions
import org.litote.kmongo.eq

abstract class IAppRoleColl<CC : ICommonContainer<T, ID, FILT>, T : IAppRole<ID>, ID : Any, FILT : IApiFilter<*>>(
    commonContainer: CC
) : Coll<CC, T, ID, FILT>(
    commonContainer = commonContainer
) {
    open suspend fun insertDefaultAppRole(
        roleType: RoleType,
        container: ICommonContainer<*, *, *>?,
        crudTask: CrudTask,
        classOwner: String,
        funcName: String
    ): ItemState<T> = ItemState(isOk = false)

    override suspend fun onAfterOpen() {
        coroutine.ensureUniqueIndex(
            IAppRole<*>::classOwner,
            indexOptions = IndexOptions().partialFilterExpression(IAppRole<*>::roleType eq RoleType.CrudTask)
        )
        coroutine.ensureUniqueIndex(
            IAppRole<*>::classOwner,
            IAppRole<*>::funcName,
            indexOptions = IndexOptions().partialFilterExpression(IAppRole<*>::roleType eq RoleType.SingleAction)
        )
    }
}
