package com.fonrouge.fullStack.layout

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.lib.iconCrud
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.disableTooltip
import io.kvision.core.enableTooltip
import io.kvision.html.ButtonSize
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.i18n.I18n.gettext
import io.kvision.i18n.I18n.tr
import io.kvision.navbar.*
import io.kvision.state.bind

/**
 * Creates and returns a toolbar `Navbar` with actions and controls bound to the provided `ViewList`.
 *
 * @param T The type of the data items used in the `ViewList`. Must extend `BaseDoc`.
 * @param ID The type of the identifier for the data items. Must match the `_id` property type in `BaseDoc`.
 * @param viewList The `ViewList` instance containing data items and their configurations.
 * @param minToolbarSize Determines if the toolbar should use compact labels or full labels for its actions.
 *                       If true, labels are minimized, otherwise full labels are displayed.
 * @return The constructed `Navbar` containing the toolbar with the configured actions.
 */
fun <T : BaseDoc<ID>, ID : Any> Container.toolBarList(
    viewList: ViewList<out ICommonContainer<T, ID, out IApiFilter<out Any>>, T, ID, out IApiFilter<out Any>, *>,
    minToolbarSize: Boolean = false,
): Navbar {
    val delay = 300
    return navbar(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        viewList.onUserChooseItem?.let { onUserChooseItem ->
            nav {
                button(text = "Choose item") {
                    size = ButtonSize.XSMALL
                    viewList.selectedItemObs.subscribe {
                        disabled = it == null
                        style = if (it == null) ButtonStyle.OUTLINESUCCESS else ButtonStyle.SUCCESS
                    }
                    onClick {
                        viewList.selectedItemObs.value?.let {
                            onUserChooseItem.invoke(it)
                        }
                    }
                }
            }
        }
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
                    label = if (minToolbarSize) "" else tr("Detail"),
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
            viewList.configViewItem()
                ?.let { configViewItem: ConfigViewItem<ICommonContainer<T, ID, out IApiFilter<out Any>>, T, ID, *, out IApiFilter<out Any>, *> ->
                    if (viewList.editable()) {
                        navLink(
                            label = if (minToolbarSize) "" else tr("Create"),
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
                        navLink(
                            label = if (minToolbarSize) "" else tr("Update"),
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
                        navLink(
                            label = if (minToolbarSize) "" else tr("Delete"),
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
            navLink(if (minToolbarSize) "" else gettext("Reset columns"), icon = "fas fa-rotate") {
                enableTooltip(TooltipOptions(gettext("Reset columns"), animation = true, delay = delay))
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
