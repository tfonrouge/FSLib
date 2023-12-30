package com.fonrouge.fsLib.layout

import kotlinx.browser.window
import org.w3c.dom.events.Event

@Suppress("unused")
@OptIn(ExperimentalJsExport::class)
@JsExport
class TabulatorMenuItem(
    label: String? = null,
    var icon: String? = null,
    var disabled: Boolean? = null,
    var separator: Boolean? = null,
    var menu: TabulatorMenuItem? = null,
    var header: Boolean = false,
    var url: String? = null,
    var action: ((e: Event, c: dynamic) -> Unit),
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
    target: String? = null,
    features: String = "",
    action: ((e: Event, c: dynamic) -> Unit) = { _, _ ->
        url?.let {
            target?.let {
                window.open(url = url, target = target, features = features)
            } ?: window.open(
                url = url,
                features = features
            )
        }
    }
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
