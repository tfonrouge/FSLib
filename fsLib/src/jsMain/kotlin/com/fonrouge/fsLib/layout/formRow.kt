package com.fonrouge.fsLib.layout

import io.kvision.core.AlignItems
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.em

/**
 * Creates a row-style form group within the container.
 *
 * This method generates a `Div` element with predefined styles and allows
 * customization through an optional text label and initialization block.
 * The returned `Div` is aligned centrally by default.
 *
 * @param text Optional label text to be displayed above the row.
 * @param init An optional lambda function to further configure the created `Div`.
 * @return The created `Div` element representing the form row.
 */
@Suppress("unused")
fun Container.formRow(
    text: String? = null,
    alignItems: AlignItems? = AlignItems.BASELINE,
    init: (Div.() -> Unit)? = null
): Div {
    text?.let { content ->
        div(content = content, className = "row-label-group") {
            marginBottom = 0.75.em
        }
    }
    val div = Div(className = "row", init = init)
    div.alignItems = alignItems
    this.add(div)
    return div
}
