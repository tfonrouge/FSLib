package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.remote.CallAgent
import io.kvision.remote.JsonRpcRequest
import io.kvision.utils.Serialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Promise

/**
 * Retrieves the state of a specific item and applies a transformation function to the result.
 *
 * @param T The type of the item, which must extend [BaseDoc].
 * @param ID The type of the item ID, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, must extend [IApiFilter].
 * @param R The return type of the transformation function.
 *
 * @param apiItemFun The function representing the API call to retrieve the item state.
 * @param apiItem The item for which the state is queried.
 * @param transform A transformation function applied to the retrieved [ItemState] to produce the result.
 * @return A [Promise] containing the transformed result of type [R].
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, R : Any?> ICommonContainer<T, ID, FILT>.getItemState(
    apiItemFun: Function<*>,
    apiItem: ApiItem<T, ID, FILT>,
    transform: (ItemState<T>) -> R
): Promise<R> {
    val (url, method) = ConfigViewItem.serviceManager.requireCall(apiItemFun)
    val callAgent = CallAgent()
    val iApiItem = toIApiItem(apiItem)
    val apiItemSerialized =
        Json.encodeToString(IApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer), iApiItem)
    val paramList = listOf(apiItemSerialized)
    val data = Serialization.plain.encodeToString(
        JsonRpcRequest(
            id = 0,
            method = url,
            params = paramList
        )
    )
    return callAgent.remoteCall(url = url, data = data, method = method).then { r: dynamic ->
        val result = JSON.parse<dynamic>(r.result.unsafeCast<String>())
        transform(
            Json.decodeFromDynamic(
                ItemState.serializer(itemSerializer),
                result
            )
        )
    }
}
