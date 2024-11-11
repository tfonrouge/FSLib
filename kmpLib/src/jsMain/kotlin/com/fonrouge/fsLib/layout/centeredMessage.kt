package com.fonrouge.fsLib.layout

import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.html.Align
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.px
import io.kvision.utils.vh

fun Container.centeredMessage(message: String, height: CssSize = 75.vh, init: (Div.() -> Unit)? = null): Div {
    return div(content = message) {
        fontSize = 20.px
        this.height = height
        align = Align.CENTER
        lineHeight = 75.vh
    }.also {
        init?.invoke(it)
    }
}
