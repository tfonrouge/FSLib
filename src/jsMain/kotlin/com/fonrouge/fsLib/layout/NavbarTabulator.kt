package com.fonrouge.fsLib.layout

import io.kvision.core.BsBgColor
import io.kvision.core.Container
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarColor
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType

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
