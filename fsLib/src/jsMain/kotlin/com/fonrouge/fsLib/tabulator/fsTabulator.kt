@file:Suppress("unused")

package com.fonrouge.fsLib.tabulator

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.layout.toolBarList
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import com.fonrouge.fsLib.view.ViewItem
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.*
import io.kvision.panel.vPanel
import io.kvision.state.bind
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.js.Tabulator.RowComponent
import io.kvision.utils.px
import io.kvision.utils.vh
import kotlinx.browser.window
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event

/**
 * Configures and enhances a `TabulatorViewList` for a given `ViewList` of items.
 * Sets up additional interactions, styles, and behaviors for displaying and managing items in a tabular view.
 *
 * @param viewList The view list containing the items to be managed in the Tabulator view.
 * @param masterViewItem The optional master view item providing context for the Tabulator view.
 * @param types A set of table styles to be applied to the Tabulator, such as striped, bordered, hover, and small.
 * @param minToolbarSize Indicates whether the toolbar should be minimized.
 * @param editable An optional lambda to determine if the table is editable.
 * @param init An optional initializer lambda to customize the behaviors and configurations of the `TabulatorViewList`.
 * @return The configured `ViewList` with the Tabulator and options applied.
 */
@OptIn(InternalSerializationApi::class)
inline fun <CC : ICommonContainer<T, ID, FILT>, reified T : BaseDoc<ID>, ID : Any, reified FILT : IApiFilter<MID>, MID : Any> Container.fsTabulator(
    viewList: ViewList<CC, T, ID, FILT, MID>,
    masterViewItem: ViewItem<ICommonContainer<out BaseDoc<MID>, MID, *>, out BaseDoc<MID>, MID, *>? = null,
    tabulatorOptions: TabulatorOptions<T> = viewList.defaultTabulatorOptions(),
    types: Set<TableType> = setOf(
        TableType.STRIPED,
        TableType.BORDERED,
        TableType.HOVER,
        TableType.SMALL
    ),
    minToolbarSize: Boolean = true,
    noinline editable: (() -> Boolean)? = null,
    noinline init: (TabulatorViewList<T, ID, FILT, MID>.() -> Unit)? = null
): ViewList<CC, T, ID, FILT, MID> {
    masterViewItem?.let {
        viewList.masterViewItem = it
        viewList.crudTask = it.crudTask
    }
    editable?.let { viewList.editable = it }
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
            if (!errorState) {
                viewList.tabulator = tabulatorViewList(
                    viewList = viewList,
                    apiListBlock = apiListBlock,
                    apiListSerialize = apiListSerialize,
                    serializer = T::class.serializer(),
                    tabulatorOptions = tabulatorOptions,
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
                            viewList.tabulator?.onRowSelected?.invoke(viewList.selectedItemObs.value)
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
                        jsTabulator?.on("pageLoaded") { page: Int ->
                            onPageLoaded?.invoke(page)
                        }
                        jsTabulator?.on("tableBuilt") {
                            viewList.jsTabulatorBuilt = true
//                            viewList.loadColumnDefinitions()
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
                    border = Border(width = 5.px, color = Color("Red"))
                }
            }
        }
    }
    if (viewList.allowInstallPeriodicUpdate) {
        viewList.installUpdate()
    }
    return viewList
}
