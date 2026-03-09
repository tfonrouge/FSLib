package com.fonrouge.fullStack.layout

import com.fonrouge.fullStack.view.View
import io.kvision.core.Container

/**
 * Adds a view to the container and renders its main content.
 *
 * This method invokes the `displayPage` function of the provided view, which
 * is responsible for rendering the content within the container.
 *
 * @param view The view to be added and displayed within the container.
 * @return The container itself, allowing for method chaining.
 */
@Suppress("unused")
fun Container.addView(view: View<*, *>): Container {
    view.apply {
        startDisplayPage()
    }
    return this
}
