package com.fonrouge.fullStack.layout

import io.kvision.core.Container
import io.kvision.dropdown.AutoClose
import io.kvision.dropdown.DropDown
import io.kvision.html.Div
import io.kvision.html.TAG
import io.kvision.html.tag
import io.kvision.html.ul

/**
 * Represents a `DropEnd` component, which is a dropdown menu aligned to the end of its parent container.
 *
 * This component is typically used within a user interface to create a dropdown button that triggers
 * the opening of a menu with items aligned to the end. It allows customization of the button text,
 * menu content, and additional behavior through an initialization block.
 *
 * @constructor Creates an instance of `DropEnd`.
 * @param text The text to be displayed on the dropdown toggle button.
 * @param items A vararg list of `Container` elements that represent the items within the dropdown menu.
 * @param rich A flag indicating whether the text should be processed as rich text. Defaults to `false`.
 * @param init An optional initialization block to configure the `DropEnd` component.
 */
class DropEnd(
    text: String,
    vararg items: Container,
    rich: Boolean = false,
    init: DropEnd.() -> Unit = {}
) : Div(className = "dropend") {
    init {
        tag(type = TAG.A, content = text, rich = rich, className = "dropdown-item dropdown-toggle") {
            setAttribute("data-bs-toggle", "dropdown")
            setAttribute("data-bs-auto-close", "outside")
        }
        ul(className = "dropdown-menu dropdown-menu-end") {
            items.forEach(::add)
        }
        init(this)
    }
}

/**
 * Adds a `DropEnd` component to the current `DropDown` container.
 *
 * This method creates a dropdown menu aligned to the end of the container, with the specified
 * properties and an optional initialization block for further customization.
 *
 * @param text The text to be displayed on the dropdown toggle button.
 * @param items A variable number of `Container` elements to include as items in the dropdown menu.
 * @param rich A flag indicating whether the `text` should be processed as rich text. Defaults to `false`.
 * @param autoClose The behavior for closing the dropdown menu. Defaults to `AutoClose.OUTSIDE`.
 * @param init An optional lambda to configure the `DropEnd` component.
 * @return The created `DropEnd` instance.
 */
fun DropDown.dropEnd(
    text: String,
    vararg items: Container,
    rich: Boolean = false,
    autoClose: AutoClose = AutoClose.OUTSIDE,
    init: DropEnd.() -> Unit = {}
): DropEnd = DropEnd(text = text, items = items, rich = rich, init = init).also {
    add(it)
    if (this.autoClose != autoClose) this.autoClose = autoClose
}
