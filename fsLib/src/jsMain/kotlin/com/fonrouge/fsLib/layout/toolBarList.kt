package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.lib.iconCrud
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.tabulator.NavbarTabulator
import com.fonrouge.fsLib.tabulator.navbarTabulator
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.disableTooltip
import io.kvision.core.enableTooltip
import io.kvision.dropdown.ddLink
import io.kvision.dropdown.dropDown
import io.kvision.i18n.I18n.gettext
import io.kvision.i18n.I18n.tr
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.state.bind

/**
 * Generates a toolbar navigation list for a given `ViewList` and integrates it into a container's navigation bar.
 *
 * @param viewList The `ViewList` instance containing configuration, actions, and state for generating the toolbar.
 * @param minToolbarSize A flag indicating whether to minimize the toolbar size. Defaults to true.
 * @return A `NavbarTabulator` instance containing the configured toolbar navigation.
 */
fun <T : BaseDoc<ID>, ID : Any> Container.toolBarList(
    viewList: ViewList<*, T, ID, *, *>,
    minToolbarSize: Boolean = true,
): NavbarTabulator {
    val delay = 300
    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav {
            if (viewList.hasOffCanvasFilterView) {
                navLink(
                    label = if (minToolbarSize) "" else "Filter",
                    icon = "fas fa-filter"
                ) {
                    onClick {
                        it.preventDefault()
                        viewList.offCanvasFilter?.show()
                    }
                    enableTooltip(TooltipOptions("Filter", animation = true, delay = delay))
                }
            }
            viewList.configViewItem()?.let { configViewItem ->
                navLink(
                    label = if (minToolbarSize) "" else "Detail",
                    icon = iconCrud(CrudTask.Read),
                ) {
                    onClick {
                        it.preventDefault()
                        viewList.goActionUrl(CrudTask.Read)
                    }
                    bind(viewList.selectedItemObs) { item ->
                        if (item == null) {
                            hide()
                            disableTooltip()
                        } else {
                            show()
                            enableTooltip(
                                TooltipOptions(
                                    title = "${gettext("Detail of")} " + configViewItem.commonContainer.labelId(
                                        item
                                    ),
                                    animation = true,
                                    delay = delay
                                )
                            )
                        }
                    }
                }
            }
        }
        nav {
            viewList.configViewItem()?.let { configViewItem ->
                if (viewList.editable()) {
                    dropDown(
                        text = tr("Edit"),
                        icon = "far fa-file",
                        forNavbar = true,
                        arrowVisible = false
                    ) {
                        ddLink(
                            label = tr("Create"),
                            icon = iconCrud(CrudTask.Create),
                        ) {
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Create)
                            }
                            enableTooltip(
                                TooltipOptions(
                                    title = "Create " + configViewItem.commonContainer.labelItem,
                                    animation = true,
                                    delay = delay
                                )
                            )
                        }
                        ddLink(
                            label = tr("Update"),
                            icon = iconCrud(CrudTask.Update)
                        ) {
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Update)
                            }
                            bind(viewList.selectedItemObs) { item ->
                                if (item == null) {
                                    hide()
                                    disableTooltip()
                                } else {
                                    show()
                                    enableTooltip(
                                        TooltipOptions(
                                            title = "Update " + configViewItem.commonContainer.labelId(
                                                item
                                            ),
                                            animation = true,
                                            delay = delay
                                        )
                                    )
                                }
                            }
                        }
                        ddLink(
                            label = tr("Delete"),
                            icon = iconCrud(CrudTask.Delete)
                        ) {
                            hide()
                            onClick {
                                it.preventDefault()
                                viewList.goActionUrl(CrudTask.Delete)
                            }
                            bind(viewList.selectedItemObs) { item ->
                                if (item == null) {
                                    hide()
                                    disableTooltip()
                                } else {
                                    show()
                                    enableTooltip(
                                        TooltipOptions(
                                            title = "Delete " + configViewItem.commonContainer.labelId(
                                                item
                                            ),
                                            animation = true,
                                            delay = delay
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        with(viewList) {
            navBarOptions()
        }
        nav(rightAlign = true) {
            navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo") {
                enableTooltip(TooltipOptions("Refresh table", animation = true, delay = delay))
                onClick {
                    viewList.dataUpdate()
                }
            }
            navLink(if (minToolbarSize) "" else "Reset columns", icon = "fas fa-rotate") {
                enableTooltip(TooltipOptions("Reset columns", animation = true, delay = delay))
                onClick {
                    viewList.resetColumns()
                }
            }
            navLink(label = if (minToolbarSize) "" else "Print", icon = "fas fa-print") {
                enableTooltip(TooltipOptions("Print table", animation = true, delay = delay))
                onClick {
                    viewList.outPrint()
                }
            }
            navLink(label = if (minToolbarSize) "" else "Export", icon = "fas fa-file-export") {
                enableTooltip(TooltipOptions("Export table", animation = true, delay = delay))
                onClick {
                    viewList.outToFile()
                }
            }
        }
    }
}
