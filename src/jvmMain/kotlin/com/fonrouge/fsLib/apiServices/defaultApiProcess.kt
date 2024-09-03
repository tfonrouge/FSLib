package com.fonrouge.fsLib.apiServices

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.mongoDb.Coll

/**
 * Processes the API item using the provided functions for query and action operations.
 *
 * @param CC the type of the common container
 * @param T the type of the item document
 * @param ID the type of the item identifier
 * @param FILT the type of the API filter
 * @param coll the collection instance
 * @param queryCreate the function to create a new item with the provided query
 * @param queryRead the function to read an existing item with the provided query
 * @param queryUpdate the function to update an existing item with the provided query
 * @param queryDelete the function to delete an existing item with the provided query
 * @param actionCreate the function to create a new item with the provided action
 * @param actionUpdate the function to update an existing item with the provided action
 * @param actionDelete the function to delete an existing item with the provided action
 * @return the state of the processed item
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> IApiItem<T, ID, FILT>.defaultApiProcess(
    coll: Coll<CC, T, ID, FILT>,
    queryCreate: (suspend (ApiItem.Query.Upsert.Create<T, ID, FILT>) -> ItemState<T>)? = { ItemState(isOk = true) },
    queryRead: (suspend (ApiItem.Query.Read<T, ID, FILT>, ItemState<T>) -> ItemState<T>)? = { _, itemState ->
        itemState
    },
    queryUpdate: (suspend (ApiItem.Query.Upsert.Update<T, ID, FILT>, ItemState<T>) -> ItemState<T>)? = { _, itemState ->
        itemState
    },
    queryDelete: (suspend (ApiItem.Query.Delete<T, ID, FILT>, ItemState<T>) -> ItemState<T>)? = { _, itemState ->
        itemState.item?.let { coll.findChildrenNot(it._id) } ?: itemState
    },
    actionCreate: (suspend (ApiItem.Action.Upsert.Create<T, ID, FILT>) -> ItemState<T>)? = { coll.insertOne(it) },
    actionUpdate: (suspend (ApiItem.Action.Upsert.Update<T, ID, FILT>) -> ItemState<T>)? = { coll.updateOne(it) },
    actionDelete: (suspend (ApiItem.Action.Delete<T, ID, FILT>) -> ItemState<T>)? = { coll.deleteOne(it) },
): ItemState<T> {
    return when (val apiItem = asApiItem(coll.commonContainer)) {
        is ApiItem.Query -> when (apiItem) {
            is ApiItem.Query.Upsert.Create -> queryCreate?.invoke(apiItem) ?: ItemState(isOk = false)
            is ApiItem.Query.Read -> queryRead?.invoke(apiItem, coll.findItemState(apiItem)) ?: ItemState(isOk = false)
            is ApiItem.Query.Upsert.Update -> queryUpdate?.invoke(apiItem, coll.findItemState(apiItem)) ?: ItemState(
                isOk = false
            )

            is ApiItem.Query.Delete -> queryDelete?.invoke(apiItem, coll.findItemState(apiItem))
                ?: ItemState(isOk = false)
        }

        is ApiItem.Action -> when (apiItem) {
            is ApiItem.Action.Upsert.Create -> queryCreate?.let {
                actionCreate?.invoke(apiItem)
            } ?: ItemState(isOk = false)

            is ApiItem.Action.Upsert.Update -> queryUpdate?.let {
                actionUpdate?.invoke(apiItem)
            } ?: ItemState(isOk = false)

            is ApiItem.Action.Delete -> queryDelete?.let {
                actionDelete?.invoke(apiItem)
            } ?: ItemState(isOk = false)
        }
    }
}
