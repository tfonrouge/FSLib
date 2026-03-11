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
import kotlinx.browser.document

private var toolbarCssInjected = false

/**
 * Injects enhanced CSS for toolbar buttons with refined gradients, shadows,
 * hover micro-interactions, and a polished visual hierarchy.
 */
private fun injectToolbarCss() {
    if (toolbarCssInjected) return
    toolbarCssInjected = true
    val style = document.createElement("style")
    style.textContent = """
        /* ── FSLib Toolbar — enhanced button styling ──────────────── */

        .navbar .btn-group .btn {
            font-weight: 600;
            letter-spacing: 0.02em;
            border-radius: 6px;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
            border-width: 1.5px;
        }

        .navbar .btn-group .btn::after {
            content: '';
            position: absolute;
            inset: 0;
            opacity: 0;
            transition: opacity 0.2s ease;
            border-radius: inherit;
            pointer-events: none;
        }

        .navbar .btn-group .btn:hover:not(:disabled)::after {
            opacity: 1;
        }

        .navbar .btn-group .btn:active:not(:disabled) {
            transform: scale(0.96);
        }

        /* Grouped buttons: square inner edges, rounded outer edges */
        .navbar .btn-group .btn:not(:first-child):not(:last-child) {
            border-radius: 0;
        }
        .navbar .btn-group .btn:first-child:not(:last-child) {
            border-top-right-radius: 0;
            border-bottom-right-radius: 0;
        }
        .navbar .btn-group .btn:last-child:not(:first-child) {
            border-top-left-radius: 0;
            border-bottom-left-radius: 0;
        }
        .navbar .btn-group .btn:only-child {
            border-radius: 6px;
        }

        /* ── Primary (Detail) ──────────────────────────────────── */
        .navbar .btn-outline-primary {
            border-color: #4a7cdc;
            color: #3568c1;
        }
        .navbar .btn-outline-primary::after {
            background: linear-gradient(135deg, rgba(53,104,193,0.08), rgba(74,124,220,0.14));
        }
        .navbar .btn-outline-primary:hover:not(:disabled) {
            background: linear-gradient(135deg, #3568c1 0%, #4a7cdc 100%);
            border-color: #3568c1;
            color: #fff;
            box-shadow: 0 2px 8px rgba(53,104,193,0.30);
        }

        /* ── Success (Create / Choose) ─────────────────────────── */
        .navbar .btn-outline-success {
            border-color: #3dab5a;
            color: #2d8f48;
        }
        .navbar .btn-outline-success::after {
            background: linear-gradient(135deg, rgba(45,143,72,0.08), rgba(61,171,90,0.14));
        }
        .navbar .btn-outline-success:hover:not(:disabled) {
            background: linear-gradient(135deg, #2d8f48 0%, #3dab5a 100%);
            border-color: #2d8f48;
            color: #fff;
            box-shadow: 0 2px 8px rgba(45,143,72,0.30);
        }
        .navbar .btn-success {
            background: linear-gradient(135deg, #2d8f48 0%, #3dab5a 100%);
            border-color: #2d8f48;
            box-shadow: 0 2px 8px rgba(45,143,72,0.25);
        }
        .navbar .btn-success:hover:not(:disabled) {
            background: linear-gradient(135deg, #257a3c 0%, #2d8f48 100%);
            box-shadow: 0 3px 12px rgba(45,143,72,0.35);
        }

        /* ── Warning (Update) ──────────────────────────────────── */
        .navbar .btn-outline-warning {
            border-color: #e0a418;
            color: #b8860b;
        }
        .navbar .btn-outline-warning::after {
            background: linear-gradient(135deg, rgba(184,134,11,0.08), rgba(224,164,24,0.14));
        }
        .navbar .btn-outline-warning:hover:not(:disabled) {
            background: linear-gradient(135deg, #c99212 0%, #e0a418 100%);
            border-color: #b8860b;
            color: #fff;
            box-shadow: 0 2px 8px rgba(184,134,11,0.30);
        }

        /* ── Danger (Delete) ───────────────────────────────────── */
        .navbar .btn-outline-danger {
            border-color: #d94452;
            color: #c0323f;
        }
        .navbar .btn-outline-danger::after {
            background: linear-gradient(135deg, rgba(192,50,63,0.08), rgba(217,68,82,0.14));
        }
        .navbar .btn-outline-danger:hover:not(:disabled) {
            background: linear-gradient(135deg, #c0323f 0%, #d94452 100%);
            border-color: #c0323f;
            color: #fff;
            box-shadow: 0 2px 8px rgba(192,50,63,0.30);
        }

        /* ── Info (Filter) ─────────────────────────────────────── */
        .navbar .btn-outline-info {
            border-color: #31a4b8;
            color: #258a9c;
        }
        .navbar .btn-outline-info::after {
            background: linear-gradient(135deg, rgba(37,138,156,0.08), rgba(49,164,184,0.14));
        }
        .navbar .btn-outline-info:hover:not(:disabled) {
            background: linear-gradient(135deg, #258a9c 0%, #31a4b8 100%);
            border-color: #258a9c;
            color: #fff;
            box-shadow: 0 2px 8px rgba(37,138,156,0.30);
        }

        /* ── Secondary (Utilities: Refresh, Reset, Print, Export) */
        .navbar .btn-outline-secondary {
            border-color: #8c95a1;
            color: #5a6370;
        }
        .navbar .btn-outline-secondary::after {
            background: linear-gradient(135deg, rgba(90,99,112,0.06), rgba(140,149,161,0.10));
        }
        .navbar .btn-outline-secondary:hover:not(:disabled) {
            background: linear-gradient(135deg, #5a6370 0%, #6c757d 100%);
            border-color: #5a6370;
            color: #fff;
            box-shadow: 0 2px 8px rgba(90,99,112,0.25);
        }

        /* ── Disabled state ────────────────────────────────────── */
        .navbar .btn-group .btn:disabled,
        .navbar .btn-group .btn.disabled {
            opacity: 0.45;
            cursor: not-allowed;
            box-shadow: none;
        }

        /* ── Icon spacing refinement ─────────────────────────── */
        .navbar .btn-group .btn i.fas,
        .navbar .btn-group .btn i.far,
        .navbar .btn-group .btn i.fa {
            transition: transform 0.2s ease;
        }
        .navbar .btn-group .btn:hover:not(:disabled) i.fas,
        .navbar .btn-group .btn:hover:not(:disabled) i.far,
        .navbar .btn-group .btn:hover:not(:disabled) i.fa {
            transform: scale(1.12);
        }
    """.trimIndent()
    document.head?.appendChild(style)
}

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
    injectToolbarCss()
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