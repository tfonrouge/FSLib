package com.fonrouge.fullStack.tabulator

import com.fonrouge.base.api.ApiList
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.layout.centeredMessage
import com.fonrouge.fullStack.layout.toolBarList
import com.fonrouge.fullStack.view.ViewDataContainer
import com.fonrouge.fullStack.view.ViewItem
import com.fonrouge.fullStack.view.ViewList
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
@Suppress("unused")
@OptIn(InternalSerializationApi::class)
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<MID>, MID : Any> Container.fsTabulator(
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
    editable: (() -> Boolean)? = null,
    init: (TabulatorViewList<CC, T, ID, FILT, MID>.() -> Unit)? = null,
): ViewList<CC, T, ID, FILT, MID> {
    masterViewItem?.let {
        viewList.masterViewItem = it
        viewList.crudTask = it.crudTask
    }
    editable?.let { viewList.editable = it }
    val apiListBlock: () -> ApiList<FILT> = {
        ApiList(apiFilter = viewList.apiFilter)
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
                viewList.tabulator = tabulatorViewList<CC, T, ID, FILT, MID>(
                    viewList = viewList,
                    apiListBlock = apiListBlock,
                    apiListSerialize = apiListSerialize,
                    serializer = viewList.configView.commonContainer.itemSerializer,
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
                        jsTabulator?.on("dataProcessed") {
                            if (oldMaxPage != getPageMax() && oldPage > getPageMax()) {
//                                console.warn("DATA PROCESSED: adjusting tabulator.page:", getPage(), "to last")
                                jsTabulator?.setPage("last")
                            }
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
