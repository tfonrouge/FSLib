package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.remote.KVServiceManager
import io.kvision.toast.Toast

/**
 * Opens a modal confirmation dialog to confirm the deletion of an item and performs the delete operation
 * if the confirmation is positive. Displays success or failure messages accordingly.
 *
 * @param T The type of the item to be deleted, extending BaseDoc.
 * @param ID The type of the item identifier, must extend Any.
 * @param FILT The type of the API filter, extending IApiFilter.
 * @param AIS The type of the API service, extending IApiCommonService.
 * @param serviceManager The service manager responsible for managing the API service instance.
 * @param apiItemFun The API function to be invoked for the item-related action.
 * @param item The item to be deleted.
 * @param apiFilter Optional parameter, a filter used while making API calls. Defaults to an instance of the appropriate filter type.
 * @param onFail Optional parameter, a callback function invoked in case of failure with the state of the action.
 * @param onSuccess Optional parameter, a callback function invoked in case of success.
 */
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> ICommonContainer<T, ID, FILT>.confirmDeleteView(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    onFail: ((ItemState<T>) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
) {
    callItemService(
        serviceManager = serviceManager,
        apiItemFun = apiItemFun,
        crudTask = CrudTask.Delete,
        callType = CallType.Query,
        id = item._id,
        item = item,
        apiFilter = apiFilter,
    ) { itemState ->
        if (itemState.hasError.not()) {
            val modal = Confirm(
                caption = "Please Confirm",
                text = "<b>Delete</b> '<i>${labelItem}</i>', id: <b>${
                    labelIdFunc(item)
                }</b> ?",
                rich = true,
                size = ModalSize.XLARGE,
                centered = true,
                noTitle = "Cancel",
                noCallback = {
                    Toast.warning("Delete canceled")
                },
                yesCallback = {
                    callItemService(
                        serviceManager = serviceManager,
                        apiItemFun = apiItemFun,
                        crudTask = CrudTask.Delete,
                        callType = CallType.Action,
                        id = item._id,
                        item = item,
                        apiFilter = apiFilter,
                    ) { itemState1 ->
                        if (itemState1.hasError.not()) {
                            Toast.success(
                                message = itemState1.msgOk ?: "Delete action successful ...",
                            )
                            onSuccess?.invoke()
                        } else {
                            Toast.warning(
                                message = itemState1.msgError ?: "Delete action failed ...",
                            )
                            onFail?.invoke(itemState1)
                        }
                        itemState1
                    }
                }
            )
            modal.show()
        } else {
            itemState.toast()
            onFail?.invoke(itemState)
        }
        itemState
    }
}
