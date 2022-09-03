@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.masterViewItemId
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.html.Link
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.types.DateSerializer
import io.kvision.utils.Serialization
import io.kvision.utils.em
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event
import kotlin.js.Date
import kotlin.js.json

inline fun <reified T : BaseModel<U>, E : IDataList, U> Container.fsTabulator(
    configView: ConfigViewList<T, out ViewList<T, E, U>, E, U>,
    masterViewItem: ViewItem<*, *>,
    minToolbarSize: Boolean = true,
    noinline stateJsonFun: (() -> kotlin.js.Json)? = null,
    noinline init: (TabulatorRemote<T, E>.() -> Unit)? = null
): Container {
    val viewList = configView.viewFunc(null)
    viewList.masterViewItem = masterViewItem
    return fsTabulator(
        viewList = viewList,
        minToolbarSize = minToolbarSize,
        stateJsonFun = stateJsonFun,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseModel<U>, E : IDataList, U> Container.fsTabulator(
    viewList: ViewList<T, E, U>,
    minToolbarSize: Boolean = true,
    noinline stateJsonFun: (() -> kotlin.js.Json)? = null,
    noinline init: (TabulatorRemote<T, E>.() -> Unit)? = null
): Container {
    val nav = toolBarList(viewList = viewList, minToolbarSize)
    val updateLinks: (item: T?) -> Unit = { item ->
        viewList.configViewItem?.let { configViewItem ->
            nav.itemId = item?._id
            nav.getChildren().forEach { component ->
                if (component is Link) {
                    when (component.id) {
                        CrudAction.Create.name -> component.url = item?._id?.let {
                            configViewItem.urlCreate + viewList.parentContextUrlParams
                        }

                        CrudAction.Update.name -> component.url = item?._id?.let {
                            configViewItem.urlUpdate(it) + viewList.parentContextUrlParams
                        }

                        CrudAction.Delete.name -> component.url = item?._id?.let {
                            configViewItem.urlDelete(it)
                        }
                    }
                }
            }
        }
    }

    val stateFunction = {
        var json: kotlin.js.Json? = null
        if (stateJsonFun != null) {
            json = stateJsonFun()
        }
        viewList.masterViewItem?.let { viewItem ->
            val json2 = json(masterViewItemId to viewItem.dataContainer.value?.item?._id)
            json = json?.add(json2) ?: json2
        }
        JSON.stringify(json)
    }

    viewList.serializer = T::class.serializer()
    viewList.module = null
    viewList.jsonHelper = Json(from = (Serialization.customConfiguration ?: Json {
        ignoreUnknownKeys = true
        isLenient = true
    })) {
        serializersModule = SerializersModule {
            contextual(Date::class, DateSerializer)
            viewList.module?.let { this.include(it) }
        }.overwriteWith(serializersModule)
    }

    viewList.tabulator = tabulatorRemote(
        serviceManager = viewList.configView.serverManager,
        function = viewList.configView.function,
        stateFunction = stateFunction,
        serializer = viewList.serializer,
        options = TabulatorOptions(
            columns = viewList.columnDefinitionList,
            height = if (viewList.masterViewItem == null) "calc(100vh - 30vh)" else null,
            layout = Layout.FITDATASTRETCH,
            layoutColumnsOnNewData = true,
            pagination = true,
            paginationMode = PaginationMode.REMOTE,
            paginationCounter = "rows",
            rowContextMenu = { viewList.contextRowMenuGenerator() },
//            paginationCounter = js(
//                """
//                function(pageSize, currentRow, currentPage, totalRows, totalPages) {
//                    return "Showing " + currentRow + " rows of " + totalRows + " total";
//                }
//                """
//            ),
            filterMode = FilterMode.REMOTE,
            sortMode = SortMode.REMOTE,
            dataLoader = false,
            dataLoaderLoading = "Loading.........",
            paginationSize = 10,
            paginationSizeSelector = true,
            autoResize = true,
        ),
    ) {
        init?.invoke(this)
        id = viewList.urlParams?.toString()
        fontSize = 0.75.em
        onEvent {
            rowSelectionChangedTabulator = {
                val item = self.getSelectedData().let {
                    if (it.isEmpty()) null else it[0]
                }
                updateLinks(item)
                viewList.onRowSelected(item)
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
                viewList.menuState = ViewList.RowContextMenuState.Opened
            }
            jsTabulator?.on("menuClosed") {
                viewList.menuState = ViewList.RowContextMenuState.Closed
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
    viewList.installUpdate(true)
    return this
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
