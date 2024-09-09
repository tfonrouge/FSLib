package com.fonrouge.fsLib.layout

import io.kvision.core.ResString
import io.kvision.dropdown.ContextMenu
import io.kvision.dropdown.cmLink
import io.kvision.dropdown.cmLinkDisabled
import io.kvision.html.Link

@Suppress("unused")
fun ContextMenu.cmLinkEnabled(
    label: String, url: String? = null, icon: String? = null, image: ResString? = null,
    className: String? = null,
    enabled: Boolean = true,
    init: (Link.() -> Unit)? = null,
): Link {
    return if (enabled) {
        cmLink(
            label = label,
            url = url,
            icon = icon,
            image = image,
            className = className,
            init = init
        )
    } else {
        cmLinkDisabled(label, icon, image, className, init)
    }
}
