package com.fonrouge.backendLib.layout

import io.kvision.core.Container
import io.kvision.html.Label

/**
 * Creates a read-only label control within the container.
 *
 * This method generates a `Label` element styled as a read-only form control. The label
 * can include optional text content, handle rich text formatting, and be associated
 * with a specific form control using the `forId` parameter. Additional configuration
 * can be applied through the `init` lambda.
 *
 * @param content Optional text or HTML content to display within the label.
 * @param rich A flag indicating whether the content should be processed as rich text.
 * @param forId The `id` of the form control that this label is associated with, if applicable.
 * @param init An optional lambda function for further customization of the label.
 * @return The created `Label` element styled as a read-only control.
 */
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
