package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.AppScope
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.state.bind
import io.kvision.tabulator.RowRangeLookup
import kotlinx.coroutines.launch

fun <T : BaseDoc<ID>, ID : Any> Container.toolBarList(
    viewList: ViewList<T, *, ID, *, *>,
    minToolbarSize: Boolean = true,
): NavbarTabulator<ID> {
    val delay = 300
    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav().bind(
            observableState = viewList.toolBarListUpdateObservable,
            removeChildren = true
        ) {
            viewList.configViewItem?.let { configViewItem ->
                linkRead = navLink(
                    label = if (minToolbarSize) "" else "Detail",
                    icon = iconCrud(CrudTask.Read),
                ) {
                    onClick {
                        it.preventDefault()
                        AppScope.launch {
                            viewList.goActionUrl(CrudTask.Read)
                        }
                    }
                    enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                }
                if (viewList.editable) {
                    navLink(
                        label = if (minToolbarSize) "" else "Create",
                        icon = iconCrud(CrudTask.Create),
                    ) {
                        onClick {
                            it.preventDefault()
                            AppScope.launch {
                                viewList.goActionUrl(CrudTask.Create)
                            }
                        }
                        enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                    }
                    linkUpdate = navLink(
                        label = if (minToolbarSize) "" else "Update",
                        icon = iconCrud(CrudTask.Update)
                    ) {
                        onClick {
                            it.preventDefault()
                            AppScope.launch {
                                viewList.goActionUrl(CrudTask.Update)
                            }
                        }
                        enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                    }
                    linkDelete = navLink(
                        label = if (minToolbarSize) "" else "Delete",
                        icon = iconCrud(CrudTask.Delete)
                    ) {
                        onClick {
                            it.preventDefault()
                            AppScope.launch {
                                viewList.goActionUrl(CrudTask.Delete)
                            }
                        }
                        enableTooltip(TooltipOptions(configViewItem.labelDelete, animation = true, delay = delay))
                    }
                }
                navLink(label = "|")
            }
            if (viewList.toolBarFilter) {
                navLink(
                    label = if (minToolbarSize) "" else "Filter",
                    icon = "fas fa-filter"
                ) {
                    onClick {
                        it.preventDefault()
                        viewList.onClickFilter()
                    }
                    enableTooltip(TooltipOptions("Filter", animation = true, delay = delay))
                }
                navLink(label = "|")
            }
            navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo").onClick {
                AppScope.launch {
                    viewList.dataUpdate()
                }
            }
            navLink(label = if (minToolbarSize) "" else "Print", icon = "fas fa-print").onClick {
                viewList.tabulator?.print(rowRangeLookup = RowRangeLookup.ALL, isStyled = true)
            }
            navLink(label = if (minToolbarSize) "" else "Export", icon = "fas fa-file-export").onClick {
                viewList.tabulator?.downloadCSV(
                    fileName = "${viewList.label}.csv",
                    dataSet = RowRangeLookup.ALL,
                    includeBOM = true
                )
            }
        }
    }
}
