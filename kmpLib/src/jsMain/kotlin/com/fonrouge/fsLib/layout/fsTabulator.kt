@file:Suppress("unused")

package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.BsBgColor
import io.kvision.core.Color
import io.kvision.core.Container
import io.kvision.core.addBsBgColor
import io.kvision.core.onEvent
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.utils.px
import io.kvision.utils.vh
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event

fun <T : BaseDoc<*>> defaultTabulatorOptions(
    tabulatorOptions: TabulatorOptions<T>,
    viewList: ViewList<*, T, *, *, *>
): TabulatorOptions<T> {
    val autoResize = tabulatorOptions.autoResize != false
    val columns = tabulatorOptions.columns ?: viewList.columnDefinitionList()
    val columnDefaults = tabulatorOptions.columnDefaults ?: viewList.columnDefaults
    val dataLoader = tabulatorOptions.dataLoader == true
    val filterMode = tabulatorOptions.filterMode ?: FilterMode.REMOTE
    val height = tabulatorOptions.height ?: "calc(100vh - 35vh)"
    val layout = tabulatorOptions.layout ?: Layout.FITDATAFILL
    val layoutColumnsOnNewData = tabulatorOptions.layoutColumnsOnNewData != false
    val pagination = tabulatorOptions.pagination != false
    val paginationCounter = tabulatorOptions.paginationCounter ?: "rows"
    val paginationMode = tabulatorOptions.paginationMode ?: PaginationMode.REMOTE
    val paginationSize = tabulatorOptions.paginationSize ?: 50
    val paginationSizeSelector = tabulatorOptions.paginationSizeSelector ?: arrayOf(10, 20, 50, 100)
    val persistenceID =
        tabulatorOptions.persistenceID ?: viewList.configView.commonContainer.itemKClass.simpleName
    val rowContextMenu = tabulatorOptions.rowContextMenu ?: { viewList.contextRowMenuGenerator() }
    val selectableRows = tabulatorOptions.selectableRows ?: 1
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
        persistenceID = persistenceID,
        rowContextMenu = rowContextMenu,
        selectableRows = selectableRows,
        sortMode = sortMode,
    )
}

inline fun <CC : ICommonContainer<T, ID, FILT>, reified T : BaseDoc<ID>, ID : Any, E : Any, reified FILT : IApiFilter<MID>, MID : Any> Container.fsTabulator(
    configViewList: ConfigViewList<CC, T, ID, out ViewList<CC, T, ID, FILT, MID>, E, FILT, MID>,
    masterViewItem: ViewItem<out ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *>? = null,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(),
    minToolbarSize: Boolean = true,
    noinline editable: (() -> Boolean)? = null,
    noinline init: (TabulatorListContainer<T, ID, FILT, MID>.() -> Unit)? = null
): ViewList<CC, T, ID, FILT, MID> {
    val viewList: ViewList<CC, T, ID, FILT, MID> = configViewList.newViewInstance(null)
    viewList.masterViewItem = masterViewItem
    editable?.let { viewList.editable = it }
    return fsTabulator(
        viewList = viewList,
        options = options,
        types = types,
        minToolbarSize = minToolbarSize,
        init = init
    )
}

@OptIn(InternalSerializationApi::class)
inline fun <CC : ICommonContainer<T, ID, FILT>, reified T : BaseDoc<ID>, ID : Any, reified FILT : IApiFilter<MID>, MID : Any> Container.fsTabulator(
    viewList: ViewList<CC, T, ID, FILT, MID>,
    options: TabulatorOptions<T> = TabulatorOptions(),
    types: Set<TableType> = setOf(
        TableType.STRIPED,
        TableType.BORDERED,
        TableType.HOVER,
        TableType.SMALL
    ),
    minToolbarSize: Boolean = true,
    noinline init: (TabulatorListContainer<T, ID, FILT, MID>.() -> Unit)? = null
): ViewList<CC, T, ID, FILT, MID> {
    val tabulatorOptions = defaultTabulatorOptions(options, viewList)
    val apiListBlock: () -> ApiList<FILT> = {
        val urlParams =
            if (viewList.masterViewItem != null) viewList.masterViewItem?.urlParams else viewList.urlParams
        val apiList: ApiList<FILT> = ApiList(apiFilter = viewList.apiFilter)
        apiList.params = JSON.stringify(urlParams?.params)
        apiList
    }

    val apiListSerialize: (ApiList<FILT>) -> String = { apiList: ApiList<FILT> ->
        Json.encodeToString(
            serializer = ApiList.serializer(viewList.configView.commonContainer.apiFilterSerializer),
            value = apiList
        )
    }

    vPanel {
        bind(viewList.errorStateObs) { errorState ->
            viewList.navbarTabulator = toolBarList(viewList = viewList, minToolbarSize)
            if (errorState == false) {
                viewList.tabulator = tabulatorListContainer(
                    viewList = viewList,
                    apiListBlock = apiListBlock,
                    apiListSerialize = apiListSerialize,
                    serializer = T::class.serializer(),
                    options = tabulatorOptions,
                    types = types,
                ) {
                    id = viewList::class.simpleName
                    init?.invoke(this)
                    onEvent {
                        rowSelectionChangedTabulator = {
                            val tList = self.getSelectedData()
                            viewList.selectedItemObs.value = tList.let {
                                if (it.isEmpty()) null else it[0]
                            }
                            viewList.onRowSelected?.invoke(viewList.selectedItemObs.value)
                        }
                        viewList.onDataLoadedTabulator?.let { func ->
                            dataLoadedTabulator = { func(it.detail.unsafeCast<List<T>>()) }
                        }
                    }
                    addAfterInsertHook {
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
            } else {
                centeredMessage(viewList.errorMessage ?: "unknown error", 50.vh) {
                    color = Color("Red")
                    addBsBgColor(BsBgColor.DARKSUBTLE)
                    border = io.kvision.core.Border(width = 5.px, color = Color("Red"))
                }
            }
        }
    }
    if (viewList.allowInstallUpdate) {
        viewList.installUpdate(true)
    }
    return viewList
}
