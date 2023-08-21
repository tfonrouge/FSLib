@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.IDataList
import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.panel.vPanel
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event

fun <T : BaseDoc<*>> defaultTabulatorOptions(
    tabulatorOptions: TabulatorOptions<T>,
    viewList: ViewList<T, *, *, *>
): TabulatorOptions<T> {
    val autoResize = tabulatorOptions.autoResize ?: true
    val columns = tabulatorOptions.columns ?: viewList.columnDefinitionList()
    val columnDefaults = tabulatorOptions.columnDefaults ?: viewList.columnDefaults
    val dataLoader = tabulatorOptions.dataLoader ?: false
    val filterMode = tabulatorOptions.filterMode ?: FilterMode.REMOTE
    val height = tabulatorOptions.height ?: "calc(100vh - 35vh)"
    val layout = tabulatorOptions.layout ?: Layout.FITDATAFILL
    val layoutColumnsOnNewData = tabulatorOptions.layoutColumnsOnNewData ?: true
    val pagination = tabulatorOptions.pagination ?: true
    val paginationCounter = tabulatorOptions.paginationCounter ?: "rows"
    val paginationMode = tabulatorOptions.paginationMode ?: PaginationMode.REMOTE
    val paginationSize = tabulatorOptions.paginationSize ?: 10
    val paginationSizeSelector = tabulatorOptions.paginationSizeSelector ?: arrayOf(10, 20, 50, 100)
    /*
        val persistence = tabulatorOptions.persistence ?: json(
            "page" to json("page" to true),
        )
    */
    val persistenceID = tabulatorOptions.persistenceID ?: viewList.configView.itemKClass.simpleName
    val rowContextMenu = tabulatorOptions.rowContextMenu ?: { viewList.contextRowMenuGenerator() }
    val selectable = tabulatorOptions.selectable ?: 1
    val sortMode = tabulatorOptions.sortMode ?: SortMode.REMOTE
    return tabulatorOptions.copy(
        autoResize = autoResize,
        columns = columns,
        columnDefaults = columnDefaults,
        dataLoader = dataLoader,
        filterMode = filterMode,
        height = height,
        layout = layout,
        layoutColumnsOnNewData = layoutColumnsOnNewData,
        pagination = pagination,
        paginationCounter = paginationCounter,
        paginationMode = paginationMode,
        paginationSize = paginationSize,
        paginationSizeSelector = paginationSizeSelector,
//        persistence = persistence,
        persistenceID = persistenceID,
        rowContextMenu = rowContextMenu,
        selectable = selectable,
        sortMode = sortMode,
    )
}

inline fun <reified T : BaseDoc<ID>, E : IDataList, ID : Any, reified FILT : ApiFilter> Container.fsTabulator(
    configViewList: ConfigViewList<T, out ViewList<T, E, ID, FILT>, E, ID, FILT>,
    masterViewItem: ViewItem<*, *, out FILT>? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    minToolbarSize: Boolean = true,
    noinline apiListUpdate: (ApiList<FILT>.() -> Unit)? = null,
    noinline onResult: ((dynamic) -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, ID, FILT>.() -> Unit)? = null
): ViewList<T, E, ID, FILT> {
    val viewList: ViewList<T, E, ID, FILT> = configViewList.newViewInstance(null)
    viewList.masterViewItem = masterViewItem
    return fsTabulator(
        viewList = viewList,
        options = options,
        types = types,
        minToolbarSize = minToolbarSize,
        apiListUpdate = apiListUpdate,
        onResult = onResult,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseDoc<ID>, E : IDataList, ID : Any, reified FILT : ApiFilter> Container.fsTabulator(
    viewList: ViewList<T, E, ID, FILT>,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(TableType.STRIPED, TableType.BORDERED, TableType.HOVER, TableType.SMALL),
    minToolbarSize: Boolean = true,
    noinline apiListUpdate: (ApiList<FILT>.() -> Unit)? = null,
    noinline onResult: ((dynamic) -> Unit)? = null,
    noinline init: (TabulatorListContainer<T, E, ID, FILT>.() -> Unit)? = null
): ViewList<T, E, ID, FILT> {
    val tabulatorOptions = defaultTabulatorOptions(options, viewList)
    val apiListBlock: () -> ApiList<FILT> = {
        val urlParams = if (viewList.masterViewItem != null) viewList.masterViewItem?.urlParams else viewList.urlParams
        val apiList: ApiList<FILT> = ApiList(apiFilter = viewList.apiFilter.value)
        apiList.params = JSON.stringify(urlParams?.params)
        apiList
    }

    val apiListSerialize: (ApiList<FILT>) -> String = { apiList: ApiList<FILT> ->
        Json.encodeToString(
            serializer = ApiList.serializer(viewList.configView.apiFilterKClass.serializer()),
            value = apiList
        )
    }

    vPanel {
        viewList.navbarTabulator = toolBarList(viewList = viewList, minToolbarSize)
        viewList.tabulator = tabulatorListContainer(
            serviceManager = viewList.configView.serviceManager,
            function = viewList.configView.function,
            apiListBlock = apiListBlock,
            apiListUpdate = apiListUpdate,
            apiListSerialize = apiListSerialize,
            onResult = onResult,
            serializer = T::class.serializer(),
            options = tabulatorOptions,
            types = types,
        ) {
            id = viewList::class.simpleName
            init?.invoke(this)
            onEvent {
                rowSelectionChangedTabulator = {
                    val tList = self.getSelectedData()
                    viewList.selectedItem = tList.let {
                        if (it.isEmpty()) null else it[0]
                    }
                    viewList.updateLinks(viewList.selectedItem, tList.size)
                    viewList.onRowSelected?.invoke(viewList.selectedItem)
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
    if (viewList.allowInstallUpdate) {
        viewList.installUpdate(true)
    }
    return viewList
}
