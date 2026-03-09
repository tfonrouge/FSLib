package com.fonrouge.ssr.render

import kotlinx.html.*

/**
 * Shorthand DSL extensions for common Bootstrap 5 HTML patterns.
 * Reduces verbosity when building SSR page layouts with kotlinx.html.
 */

/** Renders a Bootstrap row `<div class="row ...">`. */
fun FlowContent.row(
    classes: String = "",
    content: DIV.() -> Unit,
) {
    div(classes = "row${if (classes.isNotBlank()) " $classes" else ""}") {
        content()
    }
}

/** Renders a Bootstrap column `<div class="col-md-{size} ...">`. */
fun FlowContent.col(
    size: Int = 12,
    classes: String = "",
    content: DIV.() -> Unit,
) {
    div(classes = "col-md-$size${if (classes.isNotBlank()) " $classes" else ""}") {
        content()
    }
}

/** Renders a Bootstrap card with an optional title. */
fun FlowContent.card(
    title: String? = null,
    classes: String = "",
    content: DIV.() -> Unit,
) {
    div(classes = "card mb-3${if (classes.isNotBlank()) " $classes" else ""}") {
        div(classes = "card-body") {
            title?.let { h5(classes = "card-title") { +it } }
            content()
        }
    }
}

/** Renders a Bootstrap alert. */
fun FlowContent.alert(
    type: String = "info",
    content: DIV.() -> Unit,
) {
    div(classes = "alert alert-$type") {
        attributes["role"] = "alert"
        content()
    }
}
