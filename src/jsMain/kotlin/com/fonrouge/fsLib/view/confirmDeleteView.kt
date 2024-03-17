package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.toast.Toast

fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> confirmDeleteView(
    item: T,
    configViewItem: ConfigViewItem<CV, T, ID, out ViewItem<CV, T, ID, FILT>, *, FILT>,
    apiFilter: FILT,
    onFail: ((ItemState<T>) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
) {
    configViewItem.callItemService(
        crudTask = CrudTask.Delete,
        callType = ApiItem.CallType.Query,
        id = item._id,
        item = item,
        apiFilter = apiFilter,
    ) { itemState ->
        if (itemState.isOk) {
            val modal = Confirm(
                caption = "Please Confirm",
                text = "<b>Delete</b> '<i>${configViewItem.label}</i>', id: <b>${
                    configViewItem.commonView.labelIdFunc(item)
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
                        callType = ApiItem.CallType.Action,
                        id = item._id,
                        item = item,
                        apiFilter = apiFilter,
                    ) { itemState1 ->
                        if (itemState1.isOk) {
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