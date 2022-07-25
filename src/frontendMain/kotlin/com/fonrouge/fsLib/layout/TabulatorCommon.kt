@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.dropdown.*
import io.kvision.html.Link
import io.kvision.tabulator.*
import io.kvision.utils.obj
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer

@OptIn(InternalSerializationApi::class)
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
            linkItemPage.url = itemId?.let { configViewItem.urlRead(it) }
            nav.getChildren().forEach { component ->
                if (component is Link) {
                    when (component.id) {
                        CrudAction.Create.name -> component.url = itemId?.let {
                            configViewItem.urlCreate + viewList.parentContextUrlParams
                        }
                        CrudAction.Update.name -> component.url = itemId?.let {
                            configViewItem.urlUpdate(it) + viewList.parentContextUrlParams
                        }
                        CrudAction.Delete.name -> component.url = itemId?.let {
                            configViewItem.urlDelete(it)
                        }
                    }
                }
            }
        }
    }

    viewList.tabulator = tabulatorRemote(
        serviceManager = viewList.serverManager,
        function = viewList.function,
        stateFunction = viewList.stateFunction,
        serializer = T::class.serializer(),
        options = TabulatorOptions(
//            height = "calc(100vh - 30vh)",
            layout = Layout.FITDATASTRETCH,
            layoutColumnsOnNewData = true,
//            tooltipsHeader = true,
//            selectable = 1,
//            rowSelected = { blockRowSelected(it, RowSelectedType.Selected) },
//            rowDeselected = { blockRowSelected(it, RowSelectedType.Deselected) },
            persistence = obj {
                page = true
                size = false
            },
//            persistenceMode = true,
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

        addAfterInsertHook {
            jsTabulator?.on("tableBuilt") {
                viewList.jsTabulatorBuilt = true
            }
            jsTabulator?.on("dataProcessing") {
                window.setTimeout(
                    {
                        val list = viewList.selectedIdList
                        if (list?.isNotEmpty() == true) {
                            jsTabulator?.getRows("")?.firstOrNull {
                                it.getData().asDynamic()["_id"] == list[0]
                            }?.select?.invoke()
                        }
                    },
                    0
                )
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
                            onClick { viewList.crudActionMap[CrudAction.Create]?.invoke(itemId, null) }
                        }
                        ddLink(
                            label = configViewItem.labelUpdate,
                            icon = "fas fa-edit",
                        ) {
                            onClick { viewList.crudActionMap[CrudAction.Update]?.invoke(itemId, null) }
                        }
                        ddLink(
                            label = configViewItem.labelDelete,
                            icon = "fas fa-trash-alt",
                        ) {
                            onClick { viewList.crudActionMap[CrudAction.Delete]?.invoke(itemId, null) }
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

    viewList.updateData(true)

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
