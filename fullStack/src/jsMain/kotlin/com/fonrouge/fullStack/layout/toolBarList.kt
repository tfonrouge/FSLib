package com.fonrouge.fullStack.layout

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.lib.iconCrud
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.disableTooltip
import io.kvision.core.enableTooltip
import io.kvision.html.ButtonSize
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.i18n.I18n.tr
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.navbar
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

        // ── Picker mode ──────────────────────────────────────────────────────
        viewList.onUserChooseItem?.let { onUserChooseItem ->
            div(className = "btn-group me-2") {
                button(text = if (minToolbarSize) "" else tr("Choose"), icon = "fas fa-hand-pointer") {
                    size = ButtonSize.SMALL
                    bind(viewList.selectedItemObs) { item ->
                        disabled = item == null
                        style = if (item == null) ButtonStyle.OUTLINESUCCESS else ButtonStyle.SUCCESS
                    }
                    onClick { viewList.selectedItemObs.value?.let { onUserChooseItem.invoke(it) } }
                }
            }
        }

        // ── Filter + Detail ──────────────────────────────────────────────────
        div(className = "btn-group me-2") {
            if (viewList.hasOffCanvasFilterView) {
                button(
                    text = if (minToolbarSize) "" else tr("Filter"),
                    icon = "fas fa-filter",
                    style = ButtonStyle.OUTLINEINFO
                ) {
                    size = ButtonSize.SMALL
                    enableTooltip(TooltipOptions(tr("Filter"), animation = true, delay = delay))
                    onClick { viewList.offCanvasFilter?.show() }
                }
            }
            viewList.configViewItem()?.let { configViewItem ->
                button(
                    text = if (minToolbarSize) "" else tr("Detail"),
                    icon = iconCrud(CrudTask.Read),
                    style = ButtonStyle.OUTLINEPRIMARY
                ) {
                    size = ButtonSize.SMALL
                    bind(viewList.selectedItemObs) { item ->
                        disabled = item == null
                        if (item != null) enableTooltip(
                            TooltipOptions(
                                title = "${tr("Detail of")} ${configViewItem.commonContainer.labelId(item)}",
                                animation = true,
                                delay = delay
                            )
                        ) else disableTooltip()
                    }
                    onClick { viewList.goActionUrl(CrudTask.Read) }
                }
            }
        }

        // ── CRUD actions ─────────────────────────────────────────────────────
        viewList.configViewItem()?.let { configViewItem ->
            if (viewList.editable()) {
                div(className = "btn-group me-2") {
                    if (viewList.showCreateInToolbar) {
                        button(
                            text = if (minToolbarSize) "" else tr("Create"),
                            icon = iconCrud(CrudTask.Create),
                            style = ButtonStyle.OUTLINESUCCESS
                        ) {
                            size = ButtonSize.SMALL
                            enableTooltip(
                                TooltipOptions(
                                    title = tr("Create") + " " + configViewItem.commonContainer.labelItem,
                                    animation = true,
                                    delay = delay
                                )
                            )
                            onClick { viewList.goActionUrl(CrudTask.Create) }
                        }
                    }
                    button(
                        text = if (minToolbarSize) "" else tr("Update"),
                        icon = iconCrud(CrudTask.Update),
                        style = ButtonStyle.OUTLINEWARNING
                    ) {
                        size = ButtonSize.SMALL
                        bind(viewList.selectedItemObs) { item ->
                            disabled = item == null
                            if (item != null) enableTooltip(
                                TooltipOptions(
                                    title = tr("Update") + " " + configViewItem.commonContainer.labelId(item),
                                    animation = true,
                                    delay = delay
                                )
                            ) else disableTooltip()
                        }
                        onClick { viewList.goActionUrl(CrudTask.Update) }
                    }
                    button(
                        text = if (minToolbarSize) "" else tr("Delete"),
                        icon = iconCrud(CrudTask.Delete),
                        style = ButtonStyle.OUTLINEDANGER
                    ) {
                        size = ButtonSize.SMALL
                        bind(viewList.selectedItemObs) { item ->
                            if (item == null) {
                                hide(); disableTooltip()
                            } else {
                                show()
                                enableTooltip(
                                    TooltipOptions(
                                        title = tr("Delete") + " " + configViewItem.commonContainer.labelId(item),
                                        animation = true,
                                        delay = delay
                                    )
                                )
                            }
                        }
                        onClick { viewList.goActionUrl(CrudTask.Delete) }
                    }
                }
            }
        }

        // ── Custom extensions ─────────────────────────────────────────────────
        with(viewList) { navBarOptions() }

        // ── Utilities (right-aligned) ─────────────────────────────────────────
        div(className = "btn-group ms-auto") {
            button(
                text = if (minToolbarSize) "" else tr("Refresh"),
                icon = "fas fa-redo",
                style = ButtonStyle.OUTLINESECONDARY
            ) {
                size = ButtonSize.SMALL
                enableTooltip(TooltipOptions(tr("Refresh table"), animation = true, delay = delay))
                onClick { viewList.dataUpdate() }
            }
            button(
                text = if (minToolbarSize) "" else tr("Reset"),
                icon = "fas fa-rotate",
                style = ButtonStyle.OUTLINESECONDARY
            ) {
                size = ButtonSize.SMALL
                enableTooltip(TooltipOptions(tr("Reset columns"), animation = true, delay = delay))
                onClick { viewList.resetColumns() }
            }
            button(
                text = if (minToolbarSize) "" else tr("Print"),
                icon = "fas fa-print",
                style = ButtonStyle.OUTLINESECONDARY
            ) {
                size = ButtonSize.SMALL
                enableTooltip(TooltipOptions(tr("Print table"), animation = true, delay = delay))
                onClick { viewList.outPrint() }
            }
            button(
                text = if (minToolbarSize) "" else tr("Export"),
                icon = "fas fa-file-export",
                style = ButtonStyle.OUTLINESECONDARY
            ) {
                size = ButtonSize.SMALL
                enableTooltip(TooltipOptions(tr("Export table"), animation = true, delay = delay))
                onClick { viewList.outToFile() }
            }
        }
    }
}