package com.fonrouge.fsLib.columnDefinition

import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.AppScope
import com.fonrouge.fsLib.view.ViewList
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import kotlinx.coroutines.launch

/**
 * Configures a column definition for a "delete item" action in a table view.
 * This column is used to display a delete icon and handle the corresponding delete functionality when the icon is clicked.
 *
 * @return A ColumnDefinition instance configured for handling item deletion, including UI rendering and click event logic.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ViewList<CC, T, ID, FILT, *>.columnDefinitionDeleteItem(
): ColumnDefinition<T> {
    return ColumnDefinition(
        title = "",
        field = "deleteItem",
        hozAlign = Align.CENTER,
        formatterFunction = { cell, _, _ ->
            "<i class=\"fa-solid fa-trash\"></i>"
        },
        cellClick = { evt, cell ->
            console.warn("cellClick", cell.item)
            cell.item?.let { item ->
                configViewItem?.confirmDeleteView(item = item, onSuccess = { AppScope.launch { dataUpdate() } })
            }
        }
    )
}
