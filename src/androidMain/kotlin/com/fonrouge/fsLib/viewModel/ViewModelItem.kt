package com.fonrouge.fsLib.viewModel

import androidx.lifecycle.viewModelScope
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
open class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    final override val commonContainer: CC,
    final override val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
) : ViewModelContainer<CC, T, ID, FILT>() {
    override var apiItem: ApiItem<T, ID, FILT> = commonContainer.apiItem()
}

/**
 * Method to make an API call for an item.
 *
 * @param commonContainer The common container for the item.
 * @param function The suspend function to be executed for the API call.
 * @param onSuccess The callback function to be executed when the API call is successful. It takes the commonContainer and the resulting ItemState as parameters. (optional)
 * @param onFailure The callback function to be executed when the API call fails. It takes the commonContainer and the resulting ItemState as parameters. (optional)
 * @param apiItemBuilder An optional lambda function that can be used to modify the ApiItem object before making the API call.
 */
@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ViewModelItem<*, *, *, *>.callItemApi(
    commonContainer: ICommonContainer<T, ID, FILT>,
    id: ID? = null,
    function: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>,
    onSuccess: (ICommonContainer<T, ID, FILT>.(ItemState<T>) -> Unit)? = null,
    onFailure: (ICommonContainer<T, ID, FILT>.(ItemState<T>) -> Unit)? = null,
    apiItemBuilder: (ApiItem<T, ID, FILT>.() -> ApiItem<T, ID, FILT>)? = null
) {
    val apiItem = commonContainer.apiItem(id = id)
    viewModelScope.launch {
        val itemState = function(apiItemBuilder?.let { it(apiItem) } ?: apiItem)
        if (itemState.isOk) {
            onSuccess?.invoke(commonContainer, itemState)
        } else {
            onFailure?.invoke(commonContainer, itemState)
        }
    }
}
