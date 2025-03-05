package com.fonrouge.fsLib.tabulator

import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarColor
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType

/**
 * A class that represents a tabulator for a navigation bar, inheriting from the `Navbar` class.
 *
 * @constructor Creates a new `NavbarTabulator` instance with the specified properties and initialization logic.
 * @param label The display label for the navbar tabulator.
 * @param link The link (URL) associated with the navbar tabulator.
 * @param type The type of the navigation bar, defining its style and behavior.
 * @param expand The expand configuration for the navigation bar.
 * @param nColor The color scheme used for the navbar text elements.
 * @param bgColor The background color of the navbar.
 * @param collapseOnClick A boolean flag indicating if the navbar should collapse when an item is clicked.
 * @param className Additional CSS class names to apply for styling purposes.
 * @param init An optional initialization function used to configure the `NavbarTabulator` instance.
 */
class NavbarTabulator(
    label: String?,
    link: String?,
    type: NavbarType?,
    expand: NavbarExpand?,
    nColor: NavbarColor,
    bgColor: BsBgColor,
    collapseOnClick: Boolean,
    className: String?,
    init: (NavbarTabulator.() -> Unit)?,
) : Navbar(
    label = label,
    link = link,
    type = type,
    expand = expand,
    nColor = nColor,
    bgColor = bgColor,
    collapseOnClick = collapseOnClick,
    className = className
) {
    init {
        init?.invoke(this)
    }
}

/**
 * Adds a `NavbarTabulator` component to the container.
 *
 * @param label The display label for the navigation bar tabulator. Default is null.
 * @param link The hyperlink (URL) for the navigation bar tabulator. Default is null.
 * @param type The type of the navigation bar, determining its style and behavior. Default is null.
 * @param expand The expand breakpoint configuration for responsive behavior of the navigation bar. Default is `NavbarExpand.LG`.
 * @param nColor The text color scheme of the navigation bar. Default is `NavbarColor.LIGHT`.
 * @param bgColor The background color scheme of the navigation bar. Default is `BsBgColor.LIGHT`.
 * @param collapseOnClick Boolean flag indicating whether the navigation bar collapses when an item is clicked. Default is false.
 * @param className Additional CSS class for further customization. Default is null.
 * @param init An optional initialization block to configure the `NavbarTabulator`. Default is null.
 * @return The created `NavbarTabulator` instance.
 */
fun Container.navbarTabulator(
    label: String? = null,
    link: String? = null,
    type: NavbarType? = null,
    expand: NavbarExpand? = NavbarExpand.LG,
    nColor: NavbarColor = NavbarColor.LIGHT,
    bgColor: BsBgColor = BsBgColor.LIGHT,
    collapseOnClick: Boolean = false,
    className: String? = null,
    init: (NavbarTabulator.() -> Unit)? = null,
): NavbarTabulator {
    val navbar = NavbarTabulator(
        label,
        link,
        type,
        expand,
        nColor,
        bgColor,
        collapseOnClick,
        className,
        init
    )
    this.add(navbar)
    return navbar
}
