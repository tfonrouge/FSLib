package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IAppRole
import com.fonrouge.base.model.IAppRole.RoleType
import com.fonrouge.base.state.ItemState
import com.mongodb.client.model.IndexOptions
import org.litote.kmongo.eq

/**
 * This abstract class `IAppRoleColl` manages application roles within a collection and provides methods
 * to insert roles into the collection. It extends the `Coll` class by handling roles of type `IAppRole`.
 *
 * @param CC The common container type, extending `ICommonContainer` with generics `T`, `ID`, and `FILT`.
 * @param T The type of roles being managed, which must implement `IAppRole`.
 * @param ID The type of the identifier for the roles, which must be non-null.
 * @param FILT The type of API filter used for querying, which must extend `IApiFilter`.
 * @constructor Initializes the `IAppRoleColl` with the provided common container.
 *
 * @param commonContainer The common container instance used by this collection.
 */
abstract class IAppRoleColl<CC : ICommonContainer<T, ID, FILT>, T : IAppRole<ID>, ID : Any, FILT : IApiFilter<*>>(
    commonContainer: CC,
) : Coll<CC, T, ID, FILT>(
    commonContainer = commonContainer
) {
    open suspend fun insertSingleActionRole(
        classOwner: String,
        funcName: String,
    ): ItemState<T> = ItemState(isOk = false)

    open suspend fun insertCrudRole(
        container: ICommonContainer<*, *, *>,
        crudTask: CrudTask,
    ): ItemState<T> = ItemState(isOk = false)

    override suspend fun onAfterOpen() {
        coroutine.ensureIndex(IAppRole<*>::description)
        coroutine.ensureUniqueIndex(
            IAppRole<*>::roleType,
            IAppRole<*>::description,
            indexOptions = IndexOptions().collation(collation())
        )
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
