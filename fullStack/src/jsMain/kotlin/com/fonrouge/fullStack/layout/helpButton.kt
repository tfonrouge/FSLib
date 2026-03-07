package com.fonrouge.fullStack.layout

import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.offcanvas
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

private val styleRegex = Regex("<style[^>]*>[\\s\\S]*?</style>")
private val bodyRegex = Regex("<body[^>]*>([\\s\\S]*)</body>")

private var helpCssInjected = false

/**
 * Injects the CSS styles for the help FAB button and offcanvas panel into the document head.
 * Only injects once per page load.
 */
private fun injectHelpCss() {
    if (helpCssInjected) return
    helpCssInjected = true
    val style = document.createElement("style")
    style.textContent = """
        .help-fab {
            position: fixed !important;
            bottom: 24px;
            right: 24px;
            z-index: 1050;
            width: 52px;
            height: 52px;
            border-radius: 50% !important;
            font-size: 1.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .help-fab:hover {
            transform: scale(1.1);
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
        }
        .help-offcanvas {
            width: 520px !important;
            max-width: 90vw;
        }
        .help-offcanvas .offcanvas-header {
            background-color: #007bff;
            color: white;
        }
        .help-offcanvas .offcanvas-header .btn-close {
            filter: invert(1);
        }
    """.trimIndent()
    document.head?.appendChild(style)
}

/**
 * Adds a floating help button (FAB) that opens an offcanvas panel with a lazy-loaded HTML manual.
 *
 * The HTML file is fetched once on first click, and its `<style>` and `<body>` content
 * are extracted and rendered inside the offcanvas panel.
 *
 * @param caption The title shown in the offcanvas header.
 * @param url The URL of the HTML manual to fetch.
 */
fun Container.helpButton(caption: String, url: String) {
    injectHelpCss()
    val manualHtml = ObservableValue("")
    val oc = offcanvas(
        caption = caption,
        placement = OffPlacement.END,
        scrollableBody = true,
        closeButton = true,
        className = "help-offcanvas",
    ) {
        bind(manualHtml) { html ->
            if (html.isNotEmpty()) {
                div(content = html, rich = true)
            }
        }
    }
    button(
        text = "",
        icon = "fas fa-question-circle",
        style = ButtonStyle.PRIMARY,
        className = "help-fab"
    ) {
        title = caption
        onClick {
            if (manualHtml.value.isEmpty()) {
                io.kvision.core.KVScope.launch {
                    val response = window.fetch(url).await()
                    val text = response.text().await()
                    val style = styleRegex.find(text)?.value ?: ""
                    val body = bodyRegex.find(text)?.groupValues?.get(1) ?: text
                    manualHtml.value = style + body
                }
            }
            oc.show()
        }
    }
}
