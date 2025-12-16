package com.fonrouge.fullStack.layout


import io.kvision.core.Container
import io.kvision.dropdown.DropDown
import io.kvision.dropdown.Separator
import io.kvision.html.Li
import io.kvision.html.Ul

/**
 * Represents an item within a dropdown menu.
 *
 * The `DropItem` class extends the `Li` class and provides functionality for interacting
 * with dropdown menus. It configures the list item to optionally toggle the dropdown
 * when clicked, and allows customization of the item's content, appearance, and behavior.
 *
 * @constructor Creates an instance of `DropItem`.
 * @param text The text to be displayed for the dropdown item.
 * @param rich A flag indicating whether the content should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag specifying whether clicking the item should toggle the nearest parent dropdown. Defaults to `true`.
 * @param className An optional additional CSS class to apply to the dropdown item.
 * @param init A lambda function to allow additional configuration and initialization of the `Li` element.
 */
class DropItem(
    text: String,
    rich: Boolean = false,
    toggleDropDown: Boolean = true,
    className: String? = null,
    init: (Li.() -> Unit) = {}
) : Li(
    content = text,
    rich = rich,
    className = listOfNotNull(className?.takeIf { it.isNotBlank() }, "dropdown-item").toSet().joinToString(" "),
    init = init
) {
    init {
        if (toggleDropDown) {
            setEventListener<Li> {
                click = {
                    toggleNearestParentDropDown()
                }
            }
        }
    }

    private fun toggleNearestParentDropDown() {
        generateSequence(parent) { it.parent }
            .filterIsInstance<DropDown>()
            .firstOrNull()?.toggle()
    }
}

/**
 * Adds a dropdown item to the container.
 *
 * This method creates a `DropItem` instance with the specified text, styling, and behavior.
 * The item can optionally toggle the parent dropdown when clicked and allows customization
 * through an initialization block.
 *
 * @param text The text content of the dropdown item.
 * @param rich A flag indicating whether the text should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag specifying whether the item should toggle the nearest parent dropdown when clicked. Defaults to `true`.
 * @param className An optional additional CSS class for the dropdown item.
 * @param init A lambda function for additional configuration of the dropdown item's properties.
 * @return The created `DropItem` instance.
 */
fun Container.dropItem(
    text: String,
    rich: Boolean = false,
    toggleDropDown: Boolean = true,
    className: String? = null,
    init: (Li.() -> Unit) = {}
): DropItem = DropItem(
    text = text,
    rich = rich,
    toggleDropDown = toggleDropDown,
    className = className,
    init = init
).also(::add)
