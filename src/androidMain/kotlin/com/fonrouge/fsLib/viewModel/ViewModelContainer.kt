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
import com.fonrouge.fsLib.model.state.ISimpleState
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import kotlin.reflect.KSuspendFunction1

abstract class ViewModelContainer<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> :
    ViewModelBase() {
    abstract val commonContainer: CC
    abstract val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>?
    var item: T? by mutableStateOf(null)

    @Suppress("unused")
    suspend fun deleteItem(
        navHostController: NavHostController,
        id: ID,
        apiItemRefactor: ((ApiItem<T, ID, FILT>) -> ApiItem<T, ID, FILT>)? = null
    ) {
        val apiItemFun = this.itemStateFun ?: run {
            pushAlert(
                simpleState = SimpleState(
                    isOk = false,
                    state = ISimpleState.State.Error,
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
                pushAlert(
                    simpleState = itemState,
                    navHostController = navHostController
                )
                return
            }
        }

        apiItemFun.invoke(apiItem.copy(callType = ApiItem.CallType.Action)).also { itemState ->
            if (!itemState.isOk) {
                pushAlert(
                    simpleState = itemState,
                    navHostController = navHostController
                )
                return
            }
        }
    }
}
