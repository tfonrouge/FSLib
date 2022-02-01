package com.fonrouge.fslib.layout

import com.fonrouge.fslib.apiLib.KVWebManager.pageContainerWidth
import io.kvision.core.Container
import io.kvision.html.Align
import io.kvision.html.Div

fun Container.formColumn(
    columnWidth: Int,
    content: String? = null,
    rich: Boolean = false,
    align: Align? = null,
    containerWidth: String = pageContainerWidth,
    init: (Div.() -> Unit)? = null,
): Div {
    val div = Div(content,
        rich,
        align,
        className = columnWidth.let { "col-$containerWidth-$it " } + "form-group",
        init)
    this.add(div)
    return div
}
