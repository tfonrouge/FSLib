package com.fonrouge.fsLib.layout

import io.kvision.core.Container
import io.kvision.html.Label

@Suppress("unused")
fun Container.readOnlyValue(
    content: String? = null,
    rich: Boolean = false,
    forId: String? = null,
    init: (Label.() -> Unit)? = null,
): Label {
    val label =
        Label(
            content = content,
            rich = rich,
            forId = forId,
            className = "form-control readonly-control",
            init = init
        )
    this.add(label)
    return label
}
