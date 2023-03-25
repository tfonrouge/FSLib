package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.AppScope
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.tabulator.RowRangeLookup
import kotlinx.coroutines.launch

fun <T : BaseDoc<U>, U : Any> Container.toolBarList(
    viewList: ViewList<T, *, U>,
    minToolbarSize: Boolean = true,
): NavbarTabulator<U> {

    val delay = 300

    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav {
            viewList.configViewItem?.let { configViewItem ->
                linkRead = navLink(
                    label = if (minToolbarSize) "" else "Detail",
                    icon = iconCrud(CrudAction.Read),
                ) {
                    onClick {
                        it.preventDefault()
                        viewList.checkIfmasterViewItemUpdate(url)
                    }
                    enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                }
                if (viewList.editable) {
                    navLink(
                        label = if (minToolbarSize) "" else "Create",
                        icon = iconCrud(CrudAction.Create),
                        url = viewList.actionUrl(CrudAction.Create, null)
                    ) {
                        onClick {
                            it.preventDefault()
                            viewList.checkIfmasterViewItemUpdate(url)
                        }
                        enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                    }
                    linkUpdate = navLink(
                        label = if (minToolbarSize) "" else "Update",
                        icon = iconCrud(CrudAction.Update)
                    ) {
                        onClick {
                            it.preventDefault()
                            viewList.checkIfmasterViewItemUpdate(url)
                        }
                        enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                    }
                    linkDelete = navLink(
                        label = if (minToolbarSize) "" else "Delete",
                        icon = iconCrud(CrudAction.Delete)
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelDelete, animation = true, delay = delay))
                    }
                }
            }
            navLink(label = "", icon = "fas fa-ellipsis-v")
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
