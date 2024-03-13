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
    abstract var apiItem: ApiItem<T, ID, FILT>
    abstract var apiFilter: FILT
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
            SimpleState(
                isOk = false,
                msgError = "apiItemFun not initialized"
            ).pushAlert {
                navHostController.navigateUp()
            }
            return
        }
        var apiItem = commonContainer.apiItem(
            id = id,
            callType = ApiItem.CallType.Query,
            crudTask = CrudTask.Delete,
            apiFilter = apiFilter
        )
        apiItem = apiItemRefactor?.invoke(apiItem) ?: apiItem
        apiItemFun.invoke(apiItem).also { itemState ->
            if (!itemState.isOk) {
                itemState.pushAlert {
                    navHostController.navigateUp()
                }
                return
            }
        }

        apiItemFun.invoke(apiItem.copy(callType = ApiItem.CallType.Action)).also { itemState ->
            if (!itemState.isOk) {
                itemState.pushAlert {
                    navHostController.navigateUp()
                }
                return
            }
        }
    }

    @Suppress("unused")
    suspend fun makeQueryCall(
        apiItem: ApiItem<T, ID, FILT> = commonContainer.apiItem(),
        onDone: ViewModelContainer<CC, T, ID, FILT>.(ItemState<T>) -> Unit,
    ) {
        this.apiItem = apiItem
        itemAlreadyOn = null
        val itemState = itemStateFun?.let { it(apiItem) } ?: run {
            onDone(
                ItemState(
                    state = State.Error,
                    msgError = itemStateFunNotInitializedError
                )
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
        onDone(itemState)
    }

    @Suppress("unused")
    suspend fun makeActionCall(
        onDone: ViewModelContainer<CC, T, ID, FILT>.(ItemState<T>) -> Unit,
    ) {
        apiItem = apiItem.copy(
            id = item?._id,
            item = item,
            callType = ApiItem.CallType.Action,
            crudTask = if (itemAlreadyOn == true) CrudTask.Update else apiItem.crudTask
        )
        val itemState = when (apiItem.crudTask) {
            CrudTask.Create,
            CrudTask.Update,
            CrudTask.Delete -> itemStateFun?.let { it(apiItem) } ?: run {
                onDone(
                    ItemState(
                        state = State.Error,
                        msgError = itemStateFunNotInitializedError
                    )
                )
                return
            }

            CrudTask.Read -> TODO()
        }
        onDone(itemState)
    }
}

