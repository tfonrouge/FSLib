package com.fonrouge.fsLib.layout

import kotlinx.browser.window
import org.w3c.dom.events.Event

@JsExport
class TabulatorMenuItem(
    label: String? = null,
    var icon: String? = null,
    var disabled: Boolean? = null,
    var separator: Boolean? = null,
    var menu: TabulatorMenuItem? = null,
    var url: String? = null,
) {
    var label = "<li>&ensp;${icon?.let { "<i class ='$it'></i>" } ?: ""}&ensp;$label</li>"
    var blockOnClick: ((e: Event, c: dynamic) -> Unit)? = null
    var action: ((e: Event, c: dynamic) -> Unit) = { e, c ->
        blockOnClick?.let { it(e, c) }
        url?.let {
            window.location.href = it
        }
    }

    fun onClick(block: (e: Event, c: dynamic) -> Unit): TabulatorMenuItem {
        blockOnClick = block
        return this
    }
}

fun MutableList<TabulatorMenuItem>.menuItem(
    label: String? = null,
    icon: String? = null,
    disabled: Boolean? = null,
    separator: Boolean? = null,
    menu: TabulatorMenuItem? = null,
    url: String? = null,
    init: (TabulatorMenuItem.() -> Unit)? = null
): TabulatorMenuItem {
    val tabulatorMenuItem = TabulatorMenuItem(
        label = label,
        icon = icon,
        disabled = disabled,
        separator = separator,
        menu = menu,
        url = url
    )
    init?.invoke(tabulatorMenuItem)
    this.add(tabulatorMenuItem)
    return tabulatorMenuItem
}

