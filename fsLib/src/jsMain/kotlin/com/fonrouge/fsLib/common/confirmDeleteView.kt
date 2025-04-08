package com.fonrouge.fsLib.common

import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.toast.Toast

/**
 * Displays a confirmation modal to delete a specific item and performs the delete operation upon confirmation.
 *
 * This function first performs a query to validate if the item can be deleted, then shows a confirmation modal.
 * If the user confirms the operation, it proceeds with the delete action. If the process fails during any step,
 * the provided failure callback (`onFail`) is invoked. Success completion can be handled through the success callback (`onSuccess`).
 *
 * @param apiItemFun Reference to the function responsible for handling API requests specific to the item.
 * @param item The item to be deleted, of type `T`, which must extend `BaseDoc<ID>`.
 * @param apiFilter The API filter instance of type `FILT` used for querying, defaulted to a newly created instance.
 * @param onFail An optional callback invoked if the operation fails at any stage, providing the current state of the item.
 * @param onSuccess An optional callback invoked upon successful completion of the delete operation.
 */
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ICommonContainer<T, ID, FILT>.confirmDeleteView(
    apiItemFun: Function<*>,
    item: T,
    apiFilter: FILT = apiFilterInstance(),
    onFail: ((ItemState<T>) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
) {
    callItemService(
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
                    labelId(item)
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
