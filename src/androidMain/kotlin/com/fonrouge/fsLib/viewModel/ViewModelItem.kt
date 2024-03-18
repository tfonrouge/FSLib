package com.fonrouge.fsLib.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlin.reflect.KSuspendFunction1

@Suppress("unused")
abstract class ViewModelItem<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    final override val commonContainer: CC,
    val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>
) : ViewModelContainer<CC, T, ID, FILT>() {
    var item: T? by mutableStateOf(null)
    var crudTask: CrudTask by mutableStateOf(CrudTask.Read)
    var itemAlreadyOn by mutableStateOf<Boolean?>(null)
    var controlsEnabled by mutableStateOf(false)
    override var apiFilter: FILT = commonContainer.apiFilterInstance()
    suspend fun makeQueryCall(
        item: T,
        crudTask: CrudTask,
        onDone: ViewModelContainer<CC, T, ID, FILT>.(ItemState<T>) -> Unit,
    ) {
        return makeQueryCall(
            apiItem = commonContainer.apiItem(
                item = item,
                callType = ApiItem.CallType.Query,
                crudTask = crudTask,
                apiFilter = apiFilter
            ),
            onDone = onDone
        )
    }

    @Suppress("unused")
    suspend fun makeQueryCall(
        apiItem: ApiItem<T, ID, FILT>,
        onDone: ViewModelContainer<CC, T, ID, FILT>.(ItemState<T>) -> Unit,
    ) {
        item = apiItem.item
        crudTask = apiItem.crudTask
        apiFilter = apiItem.apiFilter
        itemAlreadyOn = null
        val itemState = itemStateFun(apiItem)
        if (crudTask == CrudTask.Create) {
            itemAlreadyOn = itemState.itemAlreadyOn
            if (itemAlreadyOn == true)
                crudTask = CrudTask.Update
            itemAlreadyOn = null
        }
        item = itemState.item
        controlsEnabled = when (crudTask) {
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
        val itemState = when (crudTask) {
            CrudTask.Create,
            CrudTask.Update,
            CrudTask.Delete -> itemStateFun(
                ApiItem(
                    id = item?._id,
                    item = item,
                    callType = ApiItem.CallType.Action,
                    crudTask = crudTask,
                    apiFilter = apiFilter
                )
            )

            CrudTask.Read -> TODO()
        }
        onDone(itemState)
    }
}
