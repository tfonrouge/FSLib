package com.fonrouge.fsLib.viewModel

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
abstract class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    final override val commonContainer: CC,
    final override val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
) : ViewModelContainer<CC, T, ID, FILT>() {
    override var apiItem: ApiItem<T, ID, FILT> = commonContainer.apiItem()
    override var apiFilter: FILT = commonContainer.apiFilterInstance()
}

/**
 * Calls the Item API with the specified parameters.
 *
 * @param id The ID of the item.
 * @param function The suspend function to be executed on the API item.
 * @param apiItemBuilder An optional lambda function to build the API item.
 * @param onResponse An optional lambda function to handle the API response.
 * @return The state of the item.
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.callItemApi(
    id: ID? = null,
    function: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>,
    apiItemBuilder: (ApiItem<T, ID, FILT>.() -> ApiItem<T, ID, FILT>)? = null,
    onResponse: (CC.(ItemState<T>) -> Unit)? = null,
): ItemState<T> {
    val apiItem = apiItem(id = id)
    val itemState = function(apiItemBuilder?.let { it(apiItem) } ?: apiItem)
    onResponse?.let { it(itemState) }
    return itemState
}
