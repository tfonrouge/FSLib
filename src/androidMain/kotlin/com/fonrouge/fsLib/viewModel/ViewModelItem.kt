package com.fonrouge.fsLib.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
abstract class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> :
    ViewModelContainer<CC, T, ID, FILT>() {

    var itemAlreadyOn by mutableStateOf<Boolean?>(null)
    var controlsEnabled by mutableStateOf(false)
    abstract override val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
    suspend fun makeQueryCall(
        apiItem: ApiItem<T, ID, FILT>,
        navHostController: NavHostController? = null,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        itemAlreadyOn = null
        val itemState = itemStateFun(apiItem.copy(callType = ApiItem.CallType.Query))
        if (apiItem.crudTask == CrudTask.Create) {
            itemAlreadyOn = itemState.itemAlreadyOn
        }
        item = itemState.item
        controlsEnabled = when (apiItem.crudTask) {
            CrudTask.Create -> true
            CrudTask.Read -> false
            CrudTask.Update -> true
            CrudTask.Delete -> false
        }
        if (itemState.isOk)
            onSuccess?.invoke(itemState)
        else
            onFailure?.invoke(itemState) ?: pushStateAlert(
                simpleState = itemState,
                navHostController = navHostController
            )
        onFinish?.invoke(itemState)
    }

    suspend fun makeActionCall(
        apiItem: ApiItem<T, ID, FILT>,
        navHostController: NavHostController? = null,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        val itemState = when (apiItem.crudTask) {
            CrudTask.Create,
            CrudTask.Update,
            CrudTask.Delete -> itemStateFun(
                apiItem.copy(
                    id = item?._id,
                    item = item,
                    callType = ApiItem.CallType.Action,
                    crudTask = if (itemAlreadyOn == true) CrudTask.Update else apiItem.crudTask
                )
            )

            CrudTask.Read -> ItemState<T>(isOk = true).also {
                onSuccess?.invoke(it)
            }
        }
        if (itemState.isOk)
            onSuccess?.invoke(itemState)
        else
            onFailure?.invoke(itemState) ?: pushStateAlert(
                simpleState = itemState,
                navHostController = navHostController
            )
        onFinish?.invoke(itemState)
    }

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
