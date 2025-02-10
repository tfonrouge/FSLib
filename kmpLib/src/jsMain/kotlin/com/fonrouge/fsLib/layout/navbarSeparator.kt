package com.fonrouge.fsLib.layout

import io.kvision.core.Border
import io.kvision.core.BorderStyle
import io.kvision.core.Color
import io.kvision.html.div
import io.kvision.navbar.Navbar
import io.kvision.utils.px
import io.kvision.utils.rem

/**
 * Adds a separator to the navigation bar.
 *
 * This method creates a visual separator in the navigation bar by adding a `div`
 * element with specific styles. The separator is represented as a vertical line
 * with a gray color, padding to the right, and a predefined height.
 */
@Suppress("unused")
fun Navbar.navSeparator() {
    div {
        height = 1.rem
        borderLeft = Border(width = 1.px, style = BorderStyle.SOLID, color = Color("gray"))
        paddingRight = 1.rem
    }
}
