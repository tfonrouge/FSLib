package com.fonrouge.fsLib.apiServices

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.mongoDb.Coll

/**
 * Process API operations for an item using default query and action functions.
 *
 * @param CC the type of the common container implementing [ICommonContainer]
 * @param T the type of the base document implementing [BaseDoc]
 * @param ID the type of the ID property in the base document
 * @param FILT the type of the API filter extending [IApiFilter]
 * @param coll the collection containing the items
 * @param queryCreate the query function for creating an item (optional, default function returns success)
 * @param queryRead the query function for reading an item (optional, default function reads item by ID)
 * @param queryUpdate the query function for updating an item (optional, default function updates item by ID)
 * @param queryDelete the query function for deleting an item (optional, default function deletes item by ID)
 * @param actionCreate the action function for creating an item (optional, default function inserts item into collection)
 * @param actionUpdate the action function for updating an item (optional, default function updates item in collection)
 * @param actionDelete the action function for deleting an item (optional, default function deletes item from collection)
 * @return the state of the item after the API operation
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> IApiItem<T, ID, FILT>.defaultApiProcess(
    coll: Coll<CC, T, ID, FILT>,
    queryCreate: suspend (ApiItem.Query.Upsert.Create<T, ID, FILT>) -> ItemState<T> = { ItemState(isOk = true) },
    queryRead: suspend (ApiItem.Query.Read<T, ID, FILT>) -> ItemState<T> = { coll.findItemStateById(it.id) },
    queryUpdate: suspend (ApiItem.Query.Upsert.Update<T, ID, FILT>) -> ItemState<T> = { coll.findItemStateById(it.id) },
    queryDelete: suspend (ApiItem.Query.Delete<T, ID, FILT>) -> ItemState<T> = { coll.findItemStateById(it.id) },
    actionCreate: suspend (ApiItem.Action.Upsert.Create<T, ID, FILT>) -> ItemState<T> = { coll.insertOne(it) },
    actionUpdate: suspend (ApiItem.Action.Upsert.Update<T, ID, FILT>) -> ItemState<T> = { coll.updateOne(it) },
    actionDelete: suspend (ApiItem.Action.Delete<T, ID, FILT>) -> ItemState<T> = { coll.deleteOne(it) },
): ItemState<T> {
    return when (val apiItem = asApiItem(coll.commonContainer)) {
        is ApiItem.Query -> when (apiItem) {
            is ApiItem.Query.Upsert.Create -> queryCreate(apiItem)

            is ApiItem.Query.Read -> queryRead(apiItem)
            is ApiItem.Query.Upsert.Update -> queryUpdate(apiItem)
            is ApiItem.Query.Delete -> queryDelete(apiItem)
        }

        is ApiItem.Action -> when (apiItem) {
            is ApiItem.Action.Upsert.Create -> actionCreate(apiItem)
            is ApiItem.Action.Upsert.Update -> actionUpdate(apiItem)
            is ApiItem.Action.Delete -> actionDelete(apiItem)
        }
    }
}
