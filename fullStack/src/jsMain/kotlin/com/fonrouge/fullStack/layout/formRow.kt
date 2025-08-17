package com.fonrouge.fullStack.layout

import io.kvision.core.AlignItems
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.em

/**
 * Creates a horizontal row within a container to group form elements or content.
 *
 * This method generates a `Div` element with a specified alignment and optional label.
 * Additional customization can be provided through a configurable initialization block.
 *
 * @param text The optional label text to be displayed above the form row.
 * @param alignItems The alignment of the items within the row. Defaults to `AlignItems.BASELINE`.
 * @param init A lambda function to configure the `Div` element.
 * @return The created `Div` element representing the form row.
 */
@Suppress("unused")
fun Container.formRow(
    text: String? = null,
    alignItems: AlignItems? = AlignItems.BASELINE,
    init: (Div.() -> Unit)? = null,
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
