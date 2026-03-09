package com.fonrouge.fullStack.layout

import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.html.Align
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.px
import io.kvision.utils.vh

/**
 * Creates a centered message inside a container with specified appearance.
 *
 * The message is displayed in a div with a default or custom height, aligned at the center,
 * and styled with a font size of 20 pixels and a line height equal to the container height.
 * Additional initialization for the div can be provided using the optional `init` function.
 *
 * @param message The text content to display in the message.
 * @param height The height of the container, defaults to 75% of the viewport height (75.vh).
 * @param init An optional lambda function to further customize the div element.
 * @return The created div element containing the centered message.
 */
fun Container.centeredMessage(message: String, height: CssSize = 75.vh, init: (Div.() -> Unit)? = null): Div {
    return div(content = message) {
        fontSize = 20.px
        this.height = height
        align = Align.CENTER
        lineHeight = height
    }.also {
        init?.invoke(it)
    }
}
