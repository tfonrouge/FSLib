package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.view.KVWebManager.pageContainerWidth
import io.kvision.core.Container
import io.kvision.html.Align
import io.kvision.html.Div

/**
 * Creates a column-style form group within the container.
 *
 * The method generates a `Div` element with a specified column width, content, alignment,
 * and other styling options. It allows customization through an optional initialization block.
 *
 * @param columnWidth The width of the column defined as a proportional value within a Bootstrap grid system.
 * @param content The optional text or HTML content to be included within the column.
 * @param rich A flag indicating whether the content should be processed as rich text.
 * @param align The alignment of the content within the column.
 * @param containerWidth The width of the container (e.g., `md`, `lg`) for responsive design.
 * @param className Additional CSS classes to apply to the column element.
 * @param init An optional lambda function to further configure the column element.
 * @return The created `Div` element representing the form column.
 */
@Suppress("unused")
fun Container.formColumn(
    columnWidth: Int,
    content: String? = null,
    rich: Boolean = false,
    align: Align? = null,
    containerWidth: String = pageContainerWidth,
    className: String = "",
    init: (Div.() -> Unit)? = null,
): Div {
    val div = Div(
        content = content,
        rich = rich,
        align = align,
        className = columnWidth.let { "col-$containerWidth-$it " } + "form-group $className",
        init = init
    )
    this.add(div)
    return div
}
