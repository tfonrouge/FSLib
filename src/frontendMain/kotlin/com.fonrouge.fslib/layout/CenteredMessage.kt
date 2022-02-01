package com.fonrouge.fslib.layout

import io.kvision.core.*
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.px
import io.kvision.utils.vh

fun Container.centeredMessage(message: String, init: (Div.() -> Unit)? = null): Div {
    return div(content = message) {
        fontSize = 20.px
        height = 75.vh
        border = Border(width = 1.px, BorderStyle.SOLID, Color.name(Col.GREEN))
        align = io.kvision.html.Align.CENTER
        lineHeight = 75.vh
    }.also {
        init?.invoke(it)
    }
}
