package com.fonrouge.fsLib.layout

import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.html.Link
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarColor
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType

class NavbarTabulator<U>(
    label: String?,
    link: String?,
    type: NavbarType?,
    expand: NavbarExpand?,
    nColor: NavbarColor,
    bgColor: BsBgColor,
    collapseOnClick: Boolean,
    className: String?,
    init: (NavbarTabulator<U>.() -> Unit)?,
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
    var itemId: U? = null
    var linkRead: Link? = null
    var linkUpdate: Link? = null
    var linkDelete: Link? = null

    init {
        init?.invoke(this)
    }
}

fun <U> Container.navbarTabulator(
    label: String? = null,
    link: String? = null,
    type: NavbarType? = null,
    expand: NavbarExpand? = NavbarExpand.LG,
    nColor: NavbarColor = NavbarColor.LIGHT,
    bgColor: BsBgColor = BsBgColor.LIGHT,
    collapseOnClick: Boolean = false,
    className: String? = null,
    init: (NavbarTabulator<U>.() -> Unit)? = null,
): NavbarTabulator<U> {
    val navbar = NavbarTabulator(label, link, type, expand, nColor, bgColor, collapseOnClick, className, init)
    this.add(navbar)
    return navbar
}
