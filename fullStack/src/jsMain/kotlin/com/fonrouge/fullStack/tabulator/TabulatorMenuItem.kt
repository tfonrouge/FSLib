package com.fonrouge.fullStack.tabulator

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Represents an item in a menu that can be used with a Tabulator table.
 *
 * @param label The display text for the menu item. This text will be wrapped with additional formatting if the
 * menu item is not a header.
 * @param icon The name of the icon class to be displayed alongside the label. If null, no icon will be shown.
 * @param disabled Indicates whether the menu item is disabled. If true, the item will not be clickable.
 * @param separator If true, the menu item will be rendered as a separator.
 * @param menu A nested menu associated with this menu item, allowing for hierarchical menus.
 * @param header If true, the menu item will be treated as a header, and it will not apply additional label formatting.
 * @param url An optional URL to associate with the menu item. This can be used for navigation purposes.
 * @param action A function that will be executed when the menu item is clicked. It takes two parameters:
 * - e: The event that triggered the action.
 * - c: A context object that may be dynamically provided.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class TabulatorMenuItem(
    label: String? = null,
    var icon: String? = null,
    var disabled: Boolean? = null,
    var separator: Boolean? = null,
    var menu: Array<TabulatorMenuItem>? = null,
    var header: Boolean = false,
    var url: String? = null,
    var action: ((e: Event, c: dynamic) -> Unit),
) {
    var rawLabel: String? = null
    var label: String? = null
        get() {
            return if (!header) "<li>&ensp;${icon?.let { "<i class ='$it'></i>" } ?: ""}&ensp;$rawLabel</li>" else rawLabel
        }
        set(value) {
            field = value
            rawLabel = value
        }

    init {
        this.rawLabel = label
    }
}

/**
 * Creates a `TabulatorMenuItem` with the specified parameters.
 *
 * @param label The display text for the menu item. Defaults to null.
 * @param icon The name of the icon class to be displayed alongside the label. If null, no icon will be shown. Defaults to null.
 * @param disabled Indicates whether the menu item is disabled. If true, the item will not be clickable. Defaults to null.
 * @param separator If true, the menu item will be rendered as a separator. Defaults to null.
 * @param menu A nested menu associated with this menu item, allowing for hierarchical menus. Defaults to null.
 * @param header If true, the menu item will be treated as a header, and it will not apply additional label formatting. Defaults to false.
 * @param url An optional URL to associate with the menu item. This can be used for navigation purposes. Defaults to null.
 * @param target The target window or tab where the URL should be opened. If null, the default behavior is applied. Defaults to null.
 * @param features Additional options for the window.open call, used when the URL is specified. Defaults to an empty string.
 * @param action A function that will be executed when the menu item is clicked. It takes two parameters:
 *     - `e`: The event that triggered the action.
 *     - `c`: A context object that may be dynamically provided. Defaults to a function that opens the specified URL.
 * @return A `TabulatorMenuItem` initialized with the provided parameters.
 */
fun menuItem(
    label: String? = null,
    icon: String? = null,
    disabled: Boolean? = null,
    separator: Boolean? = null,
    menu: Array<TabulatorMenuItem>? = null,
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
    },
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
    return tabulatorMenuItem
}
