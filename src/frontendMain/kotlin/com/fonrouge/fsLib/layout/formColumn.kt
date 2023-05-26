package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.view.KVWebManager.pageContainerWidth
import io.kvision.core.Container
import io.kvision.html.Align
import io.kvision.html.Div

@Suppress("unused")
fun Container.formColumn(
    columnWidth: Int,
    content: String? = null,
    rich: Boolean = false,
    align: Align? = null,
    containerWidth: String = pageContainerWidth,
    init: (Div.() -> Unit)? = null,
): Div {
    val div = Div(
        content = content,
        rich = rich,
        align = align,
        className = columnWidth.let { "col-$containerWidth-$it " } + "form-group",
        init = init
    )
    this.add(div)
    return div
}
