@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.core.onEvent
import io.kvision.panel.vPanel
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.utils.createInstance
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event
import kotlin.js.json

data class FSTabOptions(
    val height: String? = "calc(100vh - 35vh)",
    val fontSize: CssSize? = null,
    val pagination: Boolean = true,
    val paginationMode: PaginationMode = PaginationMode.REMOTE,
    val paginationSize: Int = 10,
    val paginationSizeSelector: dynamic = arrayOf(10, 20, 50, 100),
    val paginationElement: dynamic = null,
    val paginationAddRow: AddRowMode? = null,
    val paginationButtonCount: Int? = null,
    val paginationInitialPage: Int? = null,
    val paginationCounter: dynamic = "rows",
    val paginationCounterElement: dynamic = null,
)

inline fun <reified T : BaseDoc<ID>, E : IDataList, ID : Any, reified FILT : Any, STATE : Any> Container.fsTabulator(
    configViewList: ConfigViewList<T, out ViewList<T, E, ID, FILT, STATE>, E, ID, FILT, STATE>,
    masterViewItem: ViewItem<*, *, *>? = null,
    options: TabulatorOptions<T>? = null,
    types: Set<TableType> = setOf(),
    fsTabOptions: FSTabOptions? = FSTabOptions(),
    minToolbarSize: Boolean = true,
    noinline apiListUpdate: (ApiList.() -> Unit)? = null,
    noinline onResult: ((dynamic) -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, ID, FILT>.() -> Unit)? = null
): ViewList<T, E, ID, FILT, STATE> {
    val viewList = configViewList.viewFunc.js.createInstance<ViewList<T, E, ID, FILT, STATE>>(null)
    viewList.masterViewItem = masterViewItem
    return fsTabulator(
        viewList = viewList,
        options = options,
        types = types,
        fsTabOptions = fsTabOptions,
        minToolbarSize = minToolbarSize,
        apiListUpdate = apiListUpdate,
        onResult = onResult,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseDoc<ID>, E : IDataList, ID : Any, reified FILT : Any, STATE : Any> Container.fsTabulator(
    viewList: ViewList<T, E, ID, FILT, STATE>,
    options: TabulatorOptions<T>? = null,
    types: Set<TableType> = setOf(),
    fsTabOptions: FSTabOptions? = FSTabOptions(),
    minToolbarSize: Boolean = true,
    noinline apiListUpdate: (ApiList.() -> Unit)? = null,
    noinline onResult: ((dynamic) -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, ID, FILT>.() -> Unit)? = null
): ViewList<T, E, ID, FILT, STATE> {
    val tabOpt: TabulatorOptions<T> = options ?: TabulatorOptions(
        columns = viewList.columnDefinitionList,
//        height = if (viewList.masterViewItem == null) "calc(100vh - 30vh)" else "calc(100vh - 50vh)",
        height = fsTabOptions?.height,
        layout = Layout.FITDATASTRETCH,
        layoutColumnsOnNewData = true,
        pagination = fsTabOptions?.pagination,
        paginationMode = PaginationMode.REMOTE,
        paginationSize = fsTabOptions?.paginationSize,
        paginationSizeSelector = fsTabOptions?.paginationSizeSelector,
        paginationElement = fsTabOptions?.paginationElement,
        paginationAddRow = fsTabOptions?.paginationAddRow,
        paginationButtonCount = fsTabOptions?.paginationButtonCount,
        paginationInitialPage = fsTabOptions?.paginationInitialPage,
        paginationCounter = fsTabOptions?.paginationCounter,
        paginationCounterElement = fsTabOptions?.paginationCounterElement,
        selectable = 1,
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

    val apiListBlock: () -> ApiList = {
        val urlParams = if (viewList.masterViewItem != null) viewList.masterViewItem?.urlParams else viewList.urlParams
        val result: ApiList = urlParams?.apiList ?: ApiList()
        viewList.masterViewItem?.let { viewItem ->
            viewItem.item?.let {
                result.contextClass = viewList.masterViewItem?.configView?.itemKClass?.simpleName
                result.contextId = viewItem.encodedId()
            }
        }
        result.params = JSON.stringify(urlParams?.params)
        result
    }

    val apiFilterSerialize: () -> String? = {
        viewList.apiFilter.value?.let { Json.encodeToString(it) }
    }

    vPanel {
        viewList.navbarTabulator = toolBarList(viewList = viewList, minToolbarSize)
        viewList.tabulator = tabulatorListContainer(
            serviceManager = viewList.configView.serviceManager,
            function = viewList.configView.function,
            apiListBlock = apiListBlock,
            apiListUpdate = apiListUpdate,
            apiFilterSerialize = apiFilterSerialize,
            onResult = onResult,
            serializer = T::class.serializer(),
            options = tabOpt,
            types = types,
        ) {
            init?.invoke(this)
            id = viewList.urlParams?.toString()
            fsTabOptions?.fontSize?.let {
                fontSize = it
            }
            onEvent {
                rowSelectionChangedTabulator = {
                    val tList = self.getSelectedData()
                    val item = tList.let {
                        if (it.isEmpty()) null else it[0]
                    }
                    viewList.updateLinks(item, tList.size)
                    viewList.onRowSelected(item)
                }
                viewList.onDataLoadedTabulator?.let { func ->
                    dataLoadedTabulator = { func(it.detail.unsafeCast<List<T>>()) }
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
