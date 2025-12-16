package com.fonrouge.fullStack.layout

import io.kvision.dropdown.Separator
import io.kvision.html.Ul

/**
 * Adds a separator element to the current unordered list (`Ul`).
 *
 * This method creates a new `Separator` instance with an optional CSS class
 * and appends it to the unordered list (`Ul`) container.
 *
 * @param className An optional CSS class to be applied to the separator element. Defaults to `null`.
 * @return The created `Separator` element.
 */
fun Ul.separator(
    className: String? = null
): Separator {
    val separator = Separator(className)
    this.add(separator)
    return separator
}
