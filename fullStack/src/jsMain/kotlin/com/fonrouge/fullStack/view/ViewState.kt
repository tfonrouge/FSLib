package com.fonrouge.fullStack.view

import com.fonrouge.fullStack.config.ConfigView
import com.fonrouge.base.lib.UrlParams
import io.kvision.core.Container

/**
 * Represents the state of a view, including its configuration and URL parameters.
 *
 * @property configView The configuration view associated with this state. It is an instance of the `ConfigView` class which contains metadata and helper functions related to the
 *  specific view.
 * @property urlParams URL parameters for the view. It is an instance of the `UrlParams` class, which contains key-value pairs parsed from the URL.
 */
class ViewState(
    val configView: ConfigView<*, *, *>,
    val urlParams: UrlParams?,
)

/**
 * Displays the specified view within the current container.
 *
 * This function creates a new view instance based on the provided `viewState` and configures it
 * by setting up the necessary lifecycle hooks, binding data, and rendering the view content.
 *
 * @param viewState The state of the view to be displayed, including its configuration and URL parameters.
 */
@Suppress("unused")
fun Container.showView(viewState: ViewState) {
    val view = viewState.configView.newViewInstance(viewState.urlParams)
    view.apply {
        view.updateTitle()
        startDisplayPage(mainView = true)
    }
}
