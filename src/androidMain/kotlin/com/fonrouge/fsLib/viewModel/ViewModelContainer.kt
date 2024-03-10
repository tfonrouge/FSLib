package com.fonrouge.fsLib.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import kotlin.reflect.KSuspendFunction1

abstract class ViewModelContainer<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> :
    ViewModelBase() {
    abstract val commonContainer: CC
    abstract val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>?
    var item: T? by mutableStateOf(null)
    var itemAlreadyOn by mutableStateOf<Boolean?>(null)
    var controlsEnabled by mutableStateOf(false)
    internal var apiItem: ApiItem<T, ID, FILT>? = null

    private val itemStateFunNotInitializedError by lazy {
        "${this::itemStateFun.name} not initialized"
    }

    fun apiFilterBuilder(): FILT = commonContainer.apiFilterInstance()

    @Suppress("unused")
    suspend fun deleteItem(
        navHostController: NavHostController,
        id: ID,
        apiItemRefactor: ((ApiItem<T, ID, FILT>) -> ApiItem<T, ID, FILT>)? = null
    ) {
        val apiItemFun = this.itemStateFun ?: run {
            pushStateAlert(
                simpleState = SimpleState(
                    state = State.Error,
                    msgError = "apiItemFun not initialized"
                ),
                navHostController = navHostController
            )
            return
        }
        var apiItem = commonContainer.apiItem(
            id = id,
            callType = ApiItem.CallType.Query,
            crudTask = CrudTask.Delete
        )
        apiItem = apiItemRefactor?.invoke(apiItem) ?: apiItem
        apiItemFun.invoke(apiItem).also { itemState ->
            if (!itemState.isOk) {
                pushStateAlert(
                    simpleState = itemState,
                    navHostController = navHostController
                )
                return
            }
        }

        apiItemFun.invoke(apiItem.copy(callType = ApiItem.CallType.Action)).also { itemState ->
            if (!itemState.isOk) {
                pushStateAlert(
                    simpleState = itemState,
                    navHostController = navHostController
                )
                return
            }
        }
    }

    suspend fun makeQueryCall(
        apiItem: ApiItem<T, ID, FILT>,
        navHostController: NavHostController? = null,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        this.apiItem = null
        itemAlreadyOn = null
        val itemState = itemStateFun?.let { it(apiItem) } ?: run {
            pushStateAlert(
                simpleState = SimpleState(
                    state = State.Error,
                    msgError = itemStateFunNotInitializedError
                ),
                navHostController = navHostController
            )
            return
        }
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
        if (itemState.isOk) {
            onSuccess?.invoke(itemState)
            this.apiItem = apiItem.copy(callType = ApiItem.CallType.Query)
        } else
            onFailure?.invoke(itemState) ?: pushStateAlert(
                simpleState = itemState,
                navHostController = navHostController
            )
        onFinish?.invoke(itemState)
    }

    suspend fun makeActionCall(
        navHostController: NavHostController? = null,
        onFailure: ((ItemState<T>) -> Unit)? = null,
        onSuccess: ((ItemState<T>) -> Unit)? = null,
        onFinish: ((ItemState<T>) -> Unit)? = null,
    ) {
        val apiItem = this.apiItem?.let {
            it.copy(
                id = item?._id,
                item = item,
                callType = ApiItem.CallType.Action,
                crudTask = if (itemAlreadyOn == true) CrudTask.Update else it.crudTask
            )
        } ?: run {
            pushStateAlert(
                simpleState = SimpleState(
                    state = State.Error,
                    msgError = "apiItem is null"
                ),
                navHostController = navHostController
            )
            return
        }
        val itemState = when (apiItem.crudTask) {
            CrudTask.Create,
            CrudTask.Update,
            CrudTask.Delete -> itemStateFun?.let { it(apiItem) } ?: run {
                pushStateAlert(
                    simpleState = SimpleState(
                        state = State.Error,
                        msgError = itemStateFunNotInitializedError
                    ),
                    navHostController = navHostController
                )
                return
            }

            CrudTask.Read -> TODO()
        }
        if (itemState.isOk)
            onSuccess?.invoke(itemState) ?: pushStateAlert(
                simpleState = itemState,
                navHostController = navHostController
            )
        else
            onFailure?.invoke(itemState) ?: pushStateAlert(
                simpleState = itemState,
                navHostController = navHostController
            )
        onFinish?.invoke(itemState)
    }
}

