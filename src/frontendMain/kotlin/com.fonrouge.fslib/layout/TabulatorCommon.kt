@file:Suppress("unused")

package com.fonrouge.fslib.layout

import com.fonrouge.fslib.apiLib.KVWebManager
import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.model.base.BaseContainerList
import com.fonrouge.fslib.model.base.BaseModel
import com.fonrouge.fslib.view.ViewList
import io.kvision.core.Container
import io.kvision.dropdown.*
import io.kvision.html.Link
import io.kvision.tabulator.*
import io.kvision.utils.obj
import io.kvision.utils.px

inline fun <reified T : BaseModel, reified U : BaseContainerList<T>> Container.tabulatorCommon(
    viewList: ViewList<T, U>,
    columnDefinitionList: List<ColumnDefinition<T>>,
    minToolbarSize: Boolean = true,
    noinline rowSelect: ((T?) -> Unit)? = null,
): Container {

    lateinit var linkItemPage: Link

    var item: T? = null

    val nav = toolBarList(viewList = viewList, minToolbarSize)
    viewList.blockRefresh = { KVWebManager.updateViewDataContainer(viewList) }
    nav.onClickRefresh = { KVWebManager.updateViewDataContainer(viewList) }

    val updateLinks: () -> Unit = {
        nav.item = item
        linkItemPage.url = item?.id?.let { "${viewList.configViewItem.navigoUrl}?id=${it}" }
        nav.getChildren().forEach { component ->
            if (component is Link) {
                when (component.id) {
                    ActionParam.Insert.name -> component.url = item?.id?.let {
                        viewList.configViewItem.urlWithInsert + viewList.parentContextUrlParams
                    }
                    ActionParam.Update.name -> component.url = item?.id?.let {
                        viewList.configViewItem.urlWithUpdate(it) + viewList.parentContextUrlParams
                    }
                    ActionParam.Delete.name -> component.url = item?.id?.let {
                        viewList.configViewItem.urlWithDelete(it)
                    }
                }
            }
        }
    }

    val blockRowSelected: (io.kvision.tabulator.js.Tabulator.RowComponent, RowSelectedType) -> Unit =
        { _, _ ->
            item = viewList.tabulator?.getSelectedData()?.let {
                if (it.isNotEmpty()) it[0] else null
            }
            updateLinks()
            viewList.onRowSelected(item)
            rowSelect?.invoke(item)
        }

    viewList.tabulator = tabulator(
        data = viewList.dataContainer?.list,
        options = TabulatorOptions(
//            height = "calc(100vh - 30vh)",
            layout = Layout.FITDATASTRETCH,
            layoutColumnsOnNewData = true,
//            tooltipsHeader = true,
            selectable = 1,
//            rowSelected = { blockRowSelected(it, RowSelectedType.Selected) },
//            rowDeselected = { blockRowSelected(it, RowSelectedType.Deselected) },
            pagination = true,
            paginationMode = PaginationMode.LOCAL,
            paginationSize = 10,
            paginationSizeSelector = true,
            autoResize = true,
            persistence = obj {
                sort = true
                filter = true
                group = true
//                    page = true
                columns = true
            },
            columns = columnDefinitionList,
        ),
    ) {

        id = viewList.urlWithParams

        fontSize = 12.px

        contextMenu {
            header("Menu Opciones")
            linkItemPage = cmLink(label = "Ver detalle de ${viewList.configViewItem.label}", url = "")
            if (viewList.editable) {
                dropDown("Edit", forDropDown = true, icon = "fas fa-pen") {
                    ddLink(
                        label = viewList.configViewItem.labelInsert,
                        icon = "fas fa-plus",
                    ) {
                        onClick { viewList.actionParamMap[ActionParam.Insert]?.invoke(item, null) }
                    }
                    ddLink(
                        label = viewList.configViewItem.labelUpdate,
                        icon = "fas fa-edit",
                    ) {
                        onClick { viewList.actionParamMap[ActionParam.Update]?.invoke(item, null) }
                    }
                    ddLink(
                        label = viewList.configViewItem.labelDelete,
                        icon = "fas fa-trash-alt",
                    ) {
                        onClick { viewList.actionParamMap[ActionParam.Delete]?.invoke(item, null) }
                    }
                }
            }
            viewList.contextMenu?.let {
                separator()
                it(this)
            }
        }
    }
    return this
}

enum class RowSelectedType {
    Selected,
    Deselected
}

fun <T : BaseModel> Tabulator<T>.update(list: List<T>?) {
    if (list == null) {
        clearData()
    } else {
        val selectedId = getSelectedData().let {
            if (it.isNotEmpty()) it[0].id else null
        }
        replaceData(list.toTypedArray())
        if (selectedId != null) {
            selectRow(selectedId)
        }
    }
}
