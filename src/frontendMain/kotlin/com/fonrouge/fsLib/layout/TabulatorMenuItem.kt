package com.fonrouge.fsLib.layout

import io.kvision.tabulator.js.Tabulator
import kotlinx.browser.window
import org.w3c.dom.events.Event

@JsExport
class TabulatorMenuItem(
    label: String? = null,
    var icon: String? = null,
    var disabled: Boolean? = null,
    var separator: Boolean? = null,
    var menu: TabulatorMenuItem? = null,
    var header: Boolean = false,
    var url: String? = null,
    var action: ((e: Event, c: dynamic) -> Unit)? = { e, c ->
        url?.let {
            window.location.href = it
        }
    }
) {
    var label = if (!header) "<li>&ensp;${icon?.let { "<i class ='$it'></i>" } ?: ""}&ensp;$label</li>" else label
}

fun MutableList<TabulatorMenuItem>.menuItem(
    label: String? = null,
    icon: String? = null,
    disabled: Boolean? = null,
    separator: Boolean? = null,
    menu: TabulatorMenuItem? = null,
    header: Boolean = false,
    url: String? = null,
    action: ((e: Event, c: dynamic) -> Unit)? = null
): TabulatorMenuItem {
    val tabulatorMenuItem = TabulatorMenuItem(
        label = label,
        icon = icon,
        disabled = disabled,
        separator = separator,
        menu = menu,
        header = header,
        url = url,
        action = action
    )
    this.add(tabulatorMenuItem)
    return tabulatorMenuItem
}

