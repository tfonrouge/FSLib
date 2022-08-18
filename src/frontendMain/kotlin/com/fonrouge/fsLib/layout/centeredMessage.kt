package com.fonrouge.fsLib.layout

import io.kvision.core.*
import io.kvision.html.Align
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.px
import io.kvision.utils.vh

@Suppress("unused")
fun Container.centeredMessage(message: String, init: (Div.() -> Unit)? = null): Div {
    return div(content = message) {
        fontSize = 20.px
        height = 75.vh
        align = Align.CENTER
        lineHeight = 75.vh
    }.also {
        init?.invoke(it)
    }
}
