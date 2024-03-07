package com.fonrouge.fsLib.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
abstract class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> :
    ViewModelBase() {
    private val _screenItemAlertStatus = MutableStateFlow<ItemAlert<T>?>(null)
    var screenItemAlertStatus = _screenItemAlertStatus.asStateFlow()

    var item: T? by mutableStateOf(null)
    var itemAlreadyOn by mutableStateOf<Boolean?>(null)
    var controlsEnabled by mutableStateOf(false)
    abstract val apiItemFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>

    suspend fun makeQueryCall(
        apiItem: ApiItem<T, ID, FILT>,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        val itemState = apiItemFun(apiItem.copy(callType = ApiItem.CallType.Query))
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
            onFailure?.invoke(itemState) ?: pushScreenItemAlert(itemState)
        onFinish?.invoke(itemState)
    }

    suspend fun makeActionCall(
        apiItem: ApiItem<T, ID, FILT>,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        val itemState = when (apiItem.crudTask) {
            CrudTask.Create,
            CrudTask.Update,
            CrudTask.Delete -> apiItemFun(
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
            onFailure?.invoke(itemState) ?: pushScreenItemAlert(itemState)
        onFinish?.invoke(itemState)
    }

    fun clearScreenItemAlert() {
        _screenItemAlertStatus.value = null
    }

    fun pushScreenItemAlert(
        itemState: ItemState<T>,
        canRetry: Boolean = false,
        onAccept: (() -> Unit) = {},
        onCancel: (() -> Unit) = {},
        onRetry: (() -> Unit) = {},
        onDismissRequest: (() -> Unit) = {}
    ) {
        _screenItemAlertStatus.value =
            ItemAlert(
                itemState = itemState,
                canRetry = canRetry,
                onAccept = onAccept,
                onCancel = onCancel,
                onRetry = onRetry,
                onDismissRequest = onDismissRequest
            )
    }
}

data class ItemAlert<T : BaseDoc<*>>(
    val itemState: ItemState<T>,
    val canRetry: Boolean = false,
    val onAccept: (() -> Unit) = {},
    val onCancel: (() -> Unit) = {},
    val onRetry: (() -> Unit) = {},
    val onDismissRequest: (() -> Unit) = { },
)

@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> ViewModelItem<*, *, *, *>.callItemApi(
    commonContainer: ICommonContainer<T, ID, FILT>,
    function: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>,
    onSuccess: (ICommonContainer<T, ID, FILT>.(ItemState<T>) -> Unit)? = null,
    onFailure: (ICommonContainer<T, ID, FILT>.(ItemState<T>) -> Unit)? = null,
    apiItemBuilder: () -> ApiItem<T, ID, FILT>?
) {
    apiItemBuilder()?.let { apiItem ->
        viewModelScope.launch {
            val itemState = function(apiItem)
            if (itemState.isOk) {
                onSuccess?.invoke(commonContainer, itemState)
            } else {
                onFailure?.invoke(commonContainer, itemState)
            }
        }
    }
}
