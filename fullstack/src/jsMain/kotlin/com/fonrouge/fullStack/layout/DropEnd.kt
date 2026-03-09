package com.fonrouge.fullStack.layout

import io.kvision.core.Container
import io.kvision.dropdown.AutoClose
import io.kvision.dropdown.DropDown
import io.kvision.html.*

/**
 * Represents a dropdown menu aligned to the end of the container.
 *
 * This class generates a dropdown toggle and an associated menu with end alignment.
 * The toggle is configured to handle dropdown functionality using Bootstrap's JavaScript
 * features. Custom dropdown items can be added to the menu using the initialization block.
 *
 * @constructor Creates a `DropEnd` instance.
 * @param text The text to be displayed on the dropdown toggle.
 * @param rich A flag indicating whether the toggle content should be processed as rich text. Defaults to `false`.
 * @param init A lambda function to initialize and configure the `Ul` element representing the dropdown menu.
 */
class DropEnd(
    text: String,
    rich: Boolean = false,
    className: String? = null,
    init: Ul.() -> Unit = {}
) : Div(className = "dropend") {
    init {
        tag(
            type = TAG.A,
            content = text,
            rich = rich,
            className = listOfNotNull(
                className?.takeIf { it.isNotBlank() },
                "dropdown-item dropdown-toggle"
            ).toSet().joinToString(" ")
        ) {
            setAttribute("data-bs-toggle", "dropdown")
            setAttribute("data-bs-auto-close", "outside")
        }
        ul(className = "dropdown-menu dropdown-menu-end", init = init)
    }
}

/**
 * Adds a `DropEnd` dropdown menu to the current `DropDown` container.
 *
 * This method creates a `DropEnd` instance aligned to the end, with a toggle displaying the specified text.
 * The content of the dropdown can be customized using the provided initialization block. Optionally,
 * rich text can be enabled for the toggle, and the auto-close behavior for the dropdown can be configured.
 *
 * @param text The text to be displayed on the dropdown toggle.
 * @param rich A flag indicating whether the toggle content should be processed as rich text. Defaults to `false`.
 * @param autoClose The auto-close behavior for the dropdown. Defaults to `AutoClose.OUTSIDE`.
 * @param init A lambda function to initialize and configure the dropdown menu content.
 * @return The created `DropEnd` instance.
 */
fun Container.dropEnd(
    text: String,
    rich: Boolean = false,
    autoClose: AutoClose = AutoClose.OUTSIDE,
    className: String? = null,
    init: Ul.() -> Unit = {}
): DropEnd = DropEnd(text = text, rich = rich, className = className, init = init).also {
    add(it)
    if (this is DropDown) if (this.autoClose != autoClose) this.autoClose = autoClose
}
