package com.fonrouge.fsLib.tabulator

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Represents a menu item used in a tabulator context. This class allows customization of properties
 * such as the label, icon, disabled state, separation, sub-menu, header status, URL, and an associated action.
 *
 * @property icon The icon class name to be used for the menu item. If specified, it will be rendered using an `<i>` tag.
 * @property disabled Specifies whether the menu item is disabled.
 * @property separator If set to true, this item acts as a visual separator in the menu, rather than a selectable option.
 * @property menu Specifies a sub-menu item of type `TabulatorMenuItem` associated with this menu item.
 * @property header Indicates whether this item is a header. If true, the `label` is used directly as a header title.
 * @property url Specifies a URL associated with this menu item. Clicking the item can potentially redirect to this URL.
 * @property action A lambda function called when the menu item is clicked. It receives the click `Event` and an additional context parameter.
 * @param label The caption or text displayed for this menu item. For non-header items, the label's content will be enhanced, incorporating the icon if specified.
 */
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
    var label =
        if (!header) "<li>&ensp;${icon?.let { "<i class ='$it'></i>" } ?: ""}&ensp;$label</li>" else label
}

/**
 * Adds a new `TabulatorMenuItem` to the mutable list based on the given parameters.
 *
 * @param label The text displayed for the menu item. Defaults to null.
 * @param icon The icon class name for the menu item. Defaults to null.
 * @param disabled Specifies whether the menu item is disabled. Defaults to null.
 * @param separator If true, this menu item acts as a separator rather than a selectable option. Defaults to null.
 * @param menu A sub-menu item of type `TabulatorMenuItem` to associate with this menu item. Defaults to null.
 * @param header Indicates if the menu item is a header. If true, `label` is used directly as the header title. Defaults to false.
 * @param url A URL linked to the menu item. Clicking the item redirects to this URL if provided. Defaults to null.
 * @param target The target attribute specifies where to open the linked document. Defaults to null.
 * @param features Specifies additional features of the navigation when opening a URL (e.g., window features in `window.open`). Defaults to an empty string.
 * @param action A lambda function executed when the menu item is clicked. It receives the click `Event` and an additional context parameter. Defaults to a lambda that opens the specified
 *  URL in a new or specified target window.
 * @return The newly created `TabulatorMenuItem` instance that was added to the list.
 */
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
