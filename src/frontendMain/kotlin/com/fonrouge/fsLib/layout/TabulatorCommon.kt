@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.model.base.BaseContainerList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.dropdown.*
import io.kvision.html.Link
import io.kvision.tabulator.*
import io.kvision.utils.obj
import io.kvision.utils.px

inline fun <reified T : BaseModel<*>, reified U : BaseContainerList<T>> Container.tabulatorCommon(
    viewList: ViewList<T, U>,
    columnDefinitionList: List<ColumnDefinition<T>>,
    minToolbarSize: Boolean = true,
    noinline rowSelect: ((T?) -> Unit)? = null,
): Container {

    lateinit var linkItemPage: Link

    var item: T? = null

    val nav = toolBarList(viewList = viewList, minToolbarSize)
//    viewList.blockRefresh = { KVWebManager.updateViewDataContainer(viewList) }
//    nav.onClickRefresh = { KVWebManager.updateViewDataContainer(viewList) }

    val updateLinks: () -> Unit = {
        viewList.configViewItem?.let { configViewItem ->
            nav.item = item
            linkItemPage.url = item?.id?.let { "${configViewItem.navigoUrl}?id=${it}" }
            nav.getChildren().forEach { component ->
                if (component is Link) {
                    when (component.id) {
                        ActionParam.Insert.name -> component.url = item?.id?.let {
                            configViewItem.urlWithInsert + viewList.parentContextUrlParams
                        }
                        ActionParam.Update.name -> component.url = item?.id?.let {
                            configViewItem.urlWithUpdate(it) + viewList.parentContextUrlParams
                        }
                        ActionParam.Delete.name -> component.url = item?.id?.let {
                            configViewItem.urlWithDelete(it)
                        }
                    }
                }
            }
        }

    }

    val blockRowSelected: (RowSelectedType) -> Unit =
        { _ ->
            item = viewList.tabulator?.getSelectedData()?.let {
                if (it.isNotEmpty()) it[0] else null
            }
            updateLinks()
            viewList.onRowSelected(item)
            rowSelect?.invoke(item)
        }

    viewList.tabulator = tabulator(
        data = viewList.dataContainer?.list?.toList() ?: listOf(),
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

        onEvent {
            rowSelectedTabulator = {
                blockRowSelected(RowSelectedType.Selected)
            }
            rowDeselectedTabulator = {
                blockRowSelected(RowSelectedType.Deselected)
            }
        }

        viewList.configViewItem?.let { configViewItem ->
            contextMenu {
                header("Menu Opciones")
                linkItemPage = cmLink(label = "Ver detalle de ${configViewItem.label}", url = "")
                if (viewList.editable) {
                    dropDown("Edit", forDropDown = true, icon = "fas fa-pen") {
                        ddLink(
                            label = configViewItem.labelInsert,
                            icon = "fas fa-plus",
                        ) {
                            onClick { viewList.actionParamMap[ActionParam.Insert]?.invoke(item, null) }
                        }
                        ddLink(
                            label = configViewItem.labelUpdate,
                            icon = "fas fa-edit",
                        ) {
                            onClick { viewList.actionParamMap[ActionParam.Update]?.invoke(item, null) }
                        }
                        ddLink(
                            label = configViewItem.labelDelete,
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
    }
    console.warn("viewList.tabulator =", viewList.objId, viewList.tabulator)
    return this
}

enum class RowSelectedType {
    Selected,
    Deselected
}

fun <T : BaseModel<*>> Tabulator<T>.update(list: List<T>?) {
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
