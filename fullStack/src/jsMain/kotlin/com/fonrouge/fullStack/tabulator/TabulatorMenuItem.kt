package com.fonrouge.fullStack.tabulator

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Represents a menu item that can be used within a Tabulator-based UI system. A `TabulatorMenuItem`
 * can contain a label, an optional icon, and other configurable properties such as submenus,
 * action behavior, and visual features like separators and headers.
 *
 * @constructor Initializes a TabulatorMenuItem with the provided properties.
 *
 * @param label The textual label for the menu item. Defaults to null.
 * @param icon An optional icon represented as a string, which is typically a CSS class name. Defaults to null.
 * @param disabled An optional property that indicates whether the menu item is disabled. Defaults to null.
 * @param separator An optional property to indicate whether the menu item acts as a visual separator. Defaults to null.
 * @param menu An optional array of sub-menu items (`TabulatorMenuItem`). Defaults to null.
 * @param header A boolean specifying whether the menu item is used as a header. Defaults to false.
 * @param url An optional URL associated with the menu item. Defaults to null.
 * @param action A lambda function defining the action performed when the menu item is interacted with. It takes an event and a dynamic context as arguments.
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
