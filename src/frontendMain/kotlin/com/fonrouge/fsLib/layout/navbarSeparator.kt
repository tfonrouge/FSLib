package com.fonrouge.fsLib.layout

import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.Color
import io.kvision.html.div
import io.kvision.navbar.Navbar
import io.kvision.utils.px
import io.kvision.utils.rem

@Suppress("unused")
fun Navbar.navSeparator() {
    div {
        height = 1.rem
        borderLeft = Border(width = 1.px, style = BorderStyle.SOLID, color = Color("gray"))
        paddingRight = 1.rem
    }
}
