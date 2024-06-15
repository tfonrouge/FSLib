package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.AppScope
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.dropdown.ddLink
import io.kvision.dropdown.dropDown
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.state.bind
import kotlinx.coroutines.launch

fun <T : BaseDoc<ID>, ID : Any> Container.toolBarList(
    viewList: ViewList<*, T, ID, *, *>,
    minToolbarSize: Boolean = true,
): NavbarTabulator {
    val delay = 300
    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav().bind(viewList.toolBarListUpdateObservable) {
            if (viewList.hasOffCanvasFilterView) {
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
            }
            viewList.configViewItem?.let { configViewItem ->
                linkRead = navLink(
                    label = if (minToolbarSize) "" else "Detail",
                    icon = iconCrud(CrudTask.Read),
                ) {
                    hide()
                    onClick {
                        it.preventDefault()
                        viewList.goActionUrl(CrudTask.Read)
                    }
                    enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                }
            }
        }
        nav {
            viewList.configViewItem?.let { configViewItem ->
                if (viewList.editable()) {
                    dropDown(text = "Edit", icon = "fas fa-ellipsis-vertical", forNavbar = true, arrowVisible = false) {
                        ddLink(
                            label = "Create",
                            icon = iconCrud(CrudTask.Create),
                        ) {
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Create)
                            }
                            enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                        }
                        linkUpdate = ddLink(
                            label = "Update",
                            icon = iconCrud(CrudTask.Update)
                        ) {
                            hide()
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Update)
                            }
                            enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                        }
                        linkDelete = ddLink(
                            label = "Delete",
                            icon = iconCrud(CrudTask.Delete)
                        ) {
                            hide()
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Delete)
                            }
                            enableTooltip(TooltipOptions(configViewItem.labelDelete, animation = true, delay = delay))
                        }
                    }
                }
            }
        }
        with(viewList) {
            navBarOptions()
        }
        nav(rightAlign = true) {
            navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo").onClick {
                AppScope.launch {
                    viewList.dataUpdate()
                }
            }
            navLink(label = if (minToolbarSize) "" else "Print", icon = "fas fa-print").onClick {
                viewList.outPrint()
            }
            navLink(label = if (minToolbarSize) "" else "Export", icon = "fas fa-file-export").onClick {
                viewList.outToFile()
            }
        }
    }
}
