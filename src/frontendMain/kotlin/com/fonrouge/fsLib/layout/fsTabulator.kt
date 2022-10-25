@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.ContextDataUrl
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.panel.vPanel
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.utils.createInstance
import io.kvision.utils.em
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event
import kotlin.js.json

inline fun <reified T : BaseModel<U>, E : IDataList, U : Any> Container.fsTabulator(
    configViewList: ConfigViewList<T, out ViewList<T, E, U>, E, U>,
    masterViewItem: ViewItem<*, *>,
    options: TabulatorOptions<T>? = null,
    minToolbarSize: Boolean = true,
    noinline contextDataUrl: (ContextDataUrl.() -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, U>.() -> Unit)? = null
): ViewList<T, E, U> {
    val viewList = configViewList.viewFunc.js.createInstance<ViewList<T, E, U>>(null)
    viewList.masterViewItem = masterViewItem
    return fsTabulator(
        viewList = viewList,
        options = options,
        minToolbarSize = minToolbarSize,
        contextDataUrl = contextDataUrl,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseModel<U>, E : IDataList, U : Any> Container.fsTabulator(
    viewList: ViewList<T, E, U>,
    options: TabulatorOptions<T>? = null,
    minToolbarSize: Boolean = true,
    noinline contextDataUrl: (ContextDataUrl.() -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, U>.() -> Unit)? = null
): ViewList<T, E, U> {
    val tabOpt: TabulatorOptions<T> = options ?: TabulatorOptions(
        columns = viewList.columnDefinitionList,
//        height = if (viewList.masterViewItem == null) "calc(100vh - 30vh)" else null,
        height = "calc(100vh - 30vh)",
        layout = Layout.FITDATASTRETCH,
        layoutColumnsOnNewData = true,
        pagination = true,
        paginationMode = PaginationMode.REMOTE,
        paginationCounter = "rows",
        paginationSize = 10,
        paginationSizeSelector = true,
        persistenceID = viewList.configView.itemKClass.simpleName,
        persistence = json(
            "page" to json("page" to true),
        ),
        filterMode = FilterMode.REMOTE,
        sortMode = SortMode.REMOTE,
        rowContextMenu = { viewList.contextRowMenuGenerator() },
        dataLoader = false,
//                dataLoaderLoading = "Loading.........",
        autoResize = true,
    )

    val block = {
        val urlParams = if (viewList.masterViewItem != null) viewList.masterViewItem?.urlParams else viewList.urlParams
        val result: ContextDataUrl = urlParams?.contextDataUrl ?: ContextDataUrl()
        viewList.masterViewItem?.let { viewItem ->
            viewItem.item?.let {
                result.contextClass = viewList.masterViewItem?.configView?.itemKClass?.simpleName
                result.contextId = viewItem.encodedId()
            }
        }
        result.params = JSON.stringify(urlParams?.params)
        contextDataUrl?.let {
            result.json = it(result).let { json -> JSON.stringify(json) }
        }
        result
    }

    vPanel {
        viewList.navbarTabulator = toolBarList(viewList = viewList, minToolbarSize)
        viewList.tabulator = tabulatorListContainer(
            serviceManager = viewList.configView.serviceManager,
            function = viewList.configView.function,
            contextDataUrlBlock = block,
            serializer = T::class.serializer(),
            options = tabOpt,
        ) {
            init?.invoke(this)
            id = viewList.urlParams?.toString()
            fontSize = 0.75.em
            onEvent {
                rowSelectionChangedTabulator = {
                    val tList = self.getSelectedData()
                    val item = tList.let {
                        if (it.isEmpty()) null else it[0]
                    }
                    viewList.updateLinks(item, tList.size)
                    viewList.onRowSelected(item)
                }
                rowClickTabulator = {
                    self.toggleSelectRow(it.detail.asDynamic()._row)
                    it.preventDefault()
                }
            }
            addAfterInsertHook {
                /*
                TODO: implement this in KVision
                 */
                jsTabulator?.on("rowMouseOver") { event: Event, row: RowComponent ->
                    if (!event.defaultPrevented) {
                        viewList.overItem = row.getData()
                        ViewDataContainer.clearStartTime()
                    }
                }
                jsTabulator?.on("menuOpened") {
                    viewList.menuOpenedState = true
                }
                jsTabulator?.on("menuClosed") {
                    viewList.menuOpenedState = false
                }
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
        }
    }
    viewList.installUpdate(true)
    return viewList
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
