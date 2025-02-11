package com.fonrouge.fsLib.columnDefinition

import com.fonrouge.fsLib.common.confirmDeleteView
import com.fonrouge.fsLib.commonServices.IApiCommonService
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.IApiItem
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.view.ViewList
import io.kvision.remote.KVServiceManager
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition

/**
 * Adds a column definition for a delete item action in a tabular view list.
 * This column allows users to confirm and delete an item from the list.
 *
 * @param T The type of the document managed by the column.
 * @param ID The type of the document's unique identifier.
 * @param FILT The type of filter applied while working with API items.
 * @param AIS The API common service type used for backend operations.
 * @param serviceManager A service manager to handle operations related to the API service.
 * @param apiItemFun A suspend function that defines the API interaction to delete the item, returning the state of the item.
 * @return A column definition that includes a delete action visualized as an icon, and a cell click event for deletion with a confirmation view.
 */
@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>, AIS : IApiCommonService> ViewList<*, T, ID, FILT, *>.columnDefinitionDeleteItem(
    serviceManager: KVServiceManager<AIS>,
    apiItemFun: suspend AIS.(IApiItem<T, ID, FILT>) -> ItemState<T>,
): ColumnDefinition<T> {
    return ColumnDefinition<T>(
        title = "",
        field = "deleteItem",
        hozAlign = Align.CENTER,
        formatterFunction = { _, _, _ ->
            "<i class=\"fa-solid fa-trash\"></i>"
        },
        cellClick = { _, cell ->
            cell.item?.let { item ->
                configView.commonContainer.confirmDeleteView(
                    serviceManager = serviceManager,
                    apiItemFun = apiItemFun,
                    item = item,
                    onSuccess = { dataUpdate() }
                )
            }
        }
    )
}
