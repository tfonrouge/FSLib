package com.fonrouge.base.enums

import kotlinx.serialization.Serializable

/**
 * Defines the types of help documentation available for views.
 *
 * [TUTORIAL] and [CONTEXT_HELP] are per-view files located in `help-docs/{module}/{ViewClass}/`.
 * [MANUAL] is a per-module file located in `help-docs/{module}/manual.html` — it uses a
 * sidebar layout designed for full-screen viewing and is opened in a new browser window.
 *
 * @property fileName The HTML file name for this help type.
 * @property label The display label shown in the UI tab or button.
 */
@Serializable
enum class HelpType(val fileName: String, val label: String) {
    TUTORIAL("tutorial.html", "Tutorial"),
    CONTEXT_HELP("context.html", "Ayuda Contextual"),

    /** Module-level manual — standalone document with sidebar, opened in a new window. */
    MANUAL("manual.html", "Manual del Módulo")
}
