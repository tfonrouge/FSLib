package com.fonrouge.fullStack.layout

import com.fonrouge.fullStack.view.ViewList
import io.kvision.core.Container

/**
 * Adds a page list body to the specified container using the given view list.
 *
 * @param viewList The view list object that contains the page list implementation to be added.
 */
@Suppress("unused")
fun Container.addPageListBody(viewList: ViewList<*, *, *, *>) {
    with(viewList) {
        pageListBody()
    }
}
