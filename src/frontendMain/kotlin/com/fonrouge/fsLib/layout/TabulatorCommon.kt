@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.dropdown.*
import io.kvision.html.Link
import io.kvision.tabulator.*
import io.kvision.utils.px

inline fun <reified T : BaseModel<*>, E : IDataList> Container.tabulatorCommon(
    viewList: ViewList<T, E>,
    columnDefinitionList: List<ColumnDefinition<T>>,
    minToolbarSize: Boolean = true,
    noinline rowSelect: ((Any?) -> Unit)? = null,
): Container {

    lateinit var linkItemPage: Link

    var itemId: Any? = null

    val nav = toolBarList(viewList = viewList, minToolbarSize)
//    viewList.blockRefresh = { KVWebManager.updateViewDataContainer(viewList) }
//    nav.onClickRefresh = { KVWebManager.updateViewDataContainer(viewList) }

    val updateLinks: () -> Unit = {
        viewList.configViewItem?.let { configViewItem ->
            nav.itemId = itemId
            linkItemPage.url = itemId?.let { "${configViewItem.navigoUrl}?id=${it}" }
            nav.getChildren().forEach { component ->
                if (component is Link) {
                    when (component.id) {
                        ActionParam.Insert.name -> component.url = itemId?.let {
                            configViewItem.urlWithInsert + viewList.parentContextUrlParams
                        }
                        ActionParam.Update.name -> component.url = itemId?.let {
                            configViewItem.urlWithUpdate(it) + viewList.parentContextUrlParams
                        }
                        ActionParam.Delete.name -> component.url = itemId?.let {
                            configViewItem.urlWithDelete(it)
                        }
                    }
                }
            }
        }
    }

    viewList.tabulator = tabulatorRemote(
        serviceManager = viewList.serverManager,
        function = viewList.function,
        options = TabulatorOptions(
//            height = "calc(100vh - 30vh)",
            layout = Layout.FITDATASTRETCH,
            layoutColumnsOnNewData = true,
//            tooltipsHeader = true,
            selectable = 1,
//            rowSelected = { blockRowSelected(it, RowSelectedType.Selected) },
//            rowDeselected = { blockRowSelected(it, RowSelectedType.Deselected) },
            pagination = true,
            paginationMode = PaginationMode.REMOTE,
            filterMode = FilterMode.REMOTE,
            sortMode = SortMode.REMOTE,
            dataLoader = false,
            dataLoaderLoading = "Loading.........",
            paginationSize = 10,
            paginationSizeSelector = true,
            autoResize = true,
/*
            persistence = obj {
                sort = true
                filter = true
                group = true
//                    page = true
                columns = true
            },
*/
            columns = columnDefinitionList,
        ),
    ) {

        id = viewList.urlWithParams

        fontSize = 12.px

        onEvent {
            rowSelectionChangedTabulator = {
                val item = this.self.getSelectedData().let {
                    if (it.isEmpty()) null else it[0]
                }
                itemId = item?.let { item.asDynamic()["_id"] }
                updateLinks()
                viewList.onRowSelected(itemId)
                rowSelect?.invoke(itemId)
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
                            onClick { viewList.actionParamMap[ActionParam.Insert]?.invoke(itemId, null) }
                        }
                        ddLink(
                            label = configViewItem.labelUpdate,
                            icon = "fas fa-edit",
                        ) {
                            onClick { viewList.actionParamMap[ActionParam.Update]?.invoke(itemId, null) }
                        }
                        ddLink(
                            label = configViewItem.labelDelete,
                            icon = "fas fa-trash-alt",
                        ) {
                            onClick { viewList.actionParamMap[ActionParam.Delete]?.invoke(itemId, null) }
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
            if (it.isNotEmpty()) it[0]._id else null
        }
        replaceData(list.toTypedArray())
        if (selectedId != null) {
            selectRow(selectedId)
        }
    }
}
