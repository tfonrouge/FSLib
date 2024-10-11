package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.toast.Toast

fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> confirmDeleteView(
    item: T,
    configViewItem: ConfigViewItem<CC, T, ID, out ViewItem<CC, T, ID, FILT>, *, FILT>,
    apiFilter: FILT,
    onFail: ((ItemState<T>) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
) {
    configViewItem.callItemService(
        crudTask = CrudTask.Delete,
        callType = CallType.Query,
        id = item._id,
        item = item,
        apiFilter = apiFilter,
    ) { itemState ->
        if (itemState.hasError.not()) {
            val modal = Confirm(
                caption = "Please Confirm",
                text = "<b>Delete</b> '<i>${configViewItem.label}</i>', id: <b>${
                    configViewItem.commonContainer.labelIdFunc(item)
                }</b> ?",
                rich = true,
                size = ModalSize.XLARGE,
                centered = true,
                noTitle = "Cancel",
                noCallback = {
                    Toast.warning("Delete canceled")
                },
                yesCallback = {
                    configViewItem.callItemService(
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