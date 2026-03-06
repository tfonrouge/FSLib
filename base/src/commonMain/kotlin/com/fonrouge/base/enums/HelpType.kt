package com.fonrouge.base.enums

import kotlinx.serialization.Serializable

/**
 * Defines the types of help documentation available for views.
 *
 * @property fileName The HTML file name for this help type.
 * @property label The display label shown in the UI tab or button.
 */
@Serializable
enum class HelpType(val fileName: String, val label: String) {
    TUTORIAL("tutorial.html", "Tutorial"),
    CONTEXT_HELP("context.html", "Ayuda Contextual")
}
