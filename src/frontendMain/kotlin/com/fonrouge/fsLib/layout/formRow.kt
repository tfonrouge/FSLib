package com.fonrouge.fsLib.layout

import io.kvision.core.AlignItems
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.em

@Suppress("unused")
fun Container.formRow(text: String? = null, init: Div.() -> Unit): Div {
    text?.let { content ->
        div(content = content, className = "row-label-group") {
            marginBottom = 0.75.em
        }
    }
    return div(className = "row", init = init).apply {
        alignItems = AlignItems.CENTER
    }
}
