package com.fonrouge.fullStack.layout

import io.kvision.dropdown.DropDown
import io.kvision.html.Li

/**
 * Represents a dropdown item within a dropdown menu.
 *
 * This class extends the `Li` class to provide additional functionality specific to dropdown menus.
 * It can be customized with optional text, styling, and behavior.
 *
 * @constructor Creates an instance of the `DropItem` class.
 * @param text The text to be displayed within the dropdown item.
 * @param rich Indicates whether the content should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag indicating whether the dropdown menu should be toggled when the item is clicked. Defaults to `true`.
 * @param className Additional CSS classes to apply to the dropdown item. Defaults to `null`.
 * @param init An optional initialization block to further configure the dropdown item.
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
 * Adds a dropdown item to the current `DropDown` container.
 *
 * This method creates an instance of `DropItem` with the specified parameters and adds
 * it to the dropdown menu. The dropdown item can optionally include rich text, toggle
 * the dropdown menu on click, and include additional customization.
 *
 * @param text The text to be displayed for the dropdown item.
 * @param rich Indicates whether the content should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag indicating whether the dropdown menu should be toggled when the item is clicked. Defaults to `true`.
 * @param className Additional CSS classes to apply to the dropdown item. Defaults to `null`.
 * @param init An optional lambda function to further configure the dropdown item.
 * @return The created `DropItem` instance.
 */
fun DropDown.dropItem(
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
