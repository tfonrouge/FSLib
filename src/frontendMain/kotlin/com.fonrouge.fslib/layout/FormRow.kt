package com.fonrouge.fslib.layout

import io.kvision.core.AlignItems
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.em

fun Container.formRow(text: String? = null, init: Div.() -> Unit) {
    text?.let { content ->
        div(content = content, className = "row-label-group") {
            marginBottom = 0.75.em
        }
    }
    div(className = "row", init = init).apply {
        alignItems = AlignItems.CENTER
    }
}
