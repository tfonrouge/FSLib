package com.fonrouge.fullStack.layout

import com.fonrouge.base.enums.HelpType
import com.fonrouge.fullStack.services.HelpDocsService
import io.kvision.core.Container
import io.kvision.core.KVScope
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event

private val helpStyleRegex = Regex("<style[^>]*>[\\s\\S]*?</style>")
private val helpBodyRegex = Regex("<body[^>]*>([\\s\\S]*)</body>")
private val bodySelectorRegex = Regex("""(?<=^|[},\s])body\s*(?=[{\s,])""")

private var helpButtonsCssInjected = false

/**
 * Injects the CSS styles for the help FAB button and offcanvas panel into the document head.
 * Only injects once per page load.
 */
private fun injectHelpButtonsCss() {
    if (helpButtonsCssInjected) return
    helpButtonsCssInjected = true
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
        body:has(.help-offcanvas.showing),
        body:has(.help-offcanvas.show) {
            overflow: auto !important;
            padding-right: 0 !important;
        }
        .help-offcanvas {
            width: 540px !important;
            max-width: 92vw;
            border-left: 1px solid #dee2e6;
            box-shadow: -4px 0 24px rgba(0, 0, 0, 0.08);
        }
        .help-offcanvas .offcanvas-header {
            background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
            color: white;
            display: flex;
            align-items: center;
            padding: 12px 16px;
            min-height: 48px;
        }
        .help-offcanvas .offcanvas-header .btn-close {
            filter: invert(1);
        }
        .help-offcanvas .offcanvas-header .offcanvas-title {
            flex: 1;
            font-size: 0.95rem;
            font-weight: 600;
            letter-spacing: 0.01em;
        }
        .help-offcanvas .offcanvas-body {
            padding: 0;
            background: #fdfdfe;
        }
        .help-offcanvas .offcanvas-body > .tab-panel > .nav-tabs {
            background: #f0f4f8;
            border-bottom: 1px solid #dee2e6;
            padding: 0 8px;
        }
        .help-offcanvas .offcanvas-body > .tab-panel > .nav-tabs .nav-link {
            font-size: 0.85rem;
            padding: 8px 14px;
            color: #495057;
            border: none;
            border-bottom: 2px solid transparent;
            transition: color 0.15s ease, border-color 0.15s ease;
        }
        .help-offcanvas .offcanvas-body > .tab-panel > .nav-tabs .nav-link:hover {
            color: #0d6efd;
        }
        .help-offcanvas .offcanvas-body > .tab-panel > .nav-tabs .nav-link.active {
            color: #0d6efd;
            background: transparent;
            border-bottom-color: #0d6efd;
            font-weight: 600;
        }
        .help-content-wrap {
            padding: 16px 20px 24px;
            position: relative;
        }
        .help-detach-icon {
            position: absolute;
            top: 10px;
            right: 12px;
            background: none;
            border: none;
            color: #adb5bd;
            cursor: pointer;
            font-size: 0.8rem;
            padding: 4px 6px;
            border-radius: 4px;
            transition: color 0.15s ease, background 0.15s ease;
            z-index: 1;
        }
        .help-detach-icon:hover {
            color: #0d6efd;
            background: #e7f1ff;
        }
    """.trimIndent()
    document.head?.appendChild(style)
}

/**
 * Extracts and scopes CSS styles and body content from a full HTML document.
 * Replaces `body` selectors with `.help-content-wrap` to avoid global style leaks.
 *
 * @param rawHtml The full HTML document string.
 * @return The scoped style block concatenated with the body content.
 */
private fun extractHtmlContent(rawHtml: String): String {
    val style = helpStyleRegex.find(rawHtml)?.value ?: ""
    val scopedStyle = bodySelectorRegex.replace(style, ".help-content-wrap")
    val body = helpBodyRegex.find(rawHtml)?.groupValues?.get(1) ?: rawHtml
    return scopedStyle + body
}

/**
 * Returns the FontAwesome icon class for a given [HelpType].
 */
private fun helpTypeIcon(helpType: HelpType): String = when (helpType) {
    HelpType.TUTORIAL -> "fas fa-book"
    HelpType.CONTEXT_HELP -> "fas fa-info-circle"
}

/**
 * Opens help content in a detached popup browser window.
 *
 * @param title The window title and header text.
 * @param htmlContent The HTML content to display in the popup body.
 */
private fun detachToWindow(title: String, htmlContent: String) {
    val popup = window.open("", "_blank", "width=620,height=760,scrollbars=yes,resizable=yes")
    popup?.document?.write(
        """<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$title</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
<style>
body { margin: 0; padding: 0; background: #fdfdfe; }
.detached-help-bar {
    position: sticky; top: 0; z-index: 10;
    background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
    color: white;
    padding: 10px 20px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 0.95rem; font-weight: 600;
    display: flex; align-items: center; gap: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.detached-help-bar i { opacity: 0.85; }
.detached-help-content { padding: 16px 20px 24px; }
</style>
</head>
<body>
<div class="detached-help-bar"><i class="fas fa-external-link-alt"></i> $title</div>
<div class="detached-help-content">$htmlContent</div>
</body>
</html>"""
    )
    popup?.document?.close()
}

/**
 * Renders help HTML content inside a wrapper with a detach-to-window button.
 *
 * @param helpTypeLabel The label of the help type (e.g., "Tutorial").
 * @param viewLabel The label of the view for the popup window title.
 * @param htmlContent The processed HTML content to render.
 * @param rawContent The raw HTML content used when detaching to a popup window.
 * @param onDetach Callback invoked when the user clicks the detach button (typically hides the offcanvas).
 */
private fun Container.helpContentWithDetach(
    helpTypeLabel: String,
    viewLabel: String,
    htmlContent: String,
    rawContent: String,
    onDetach: () -> Unit,
) {
    div(className = "help-content-wrap") {
        button(
            text = "",
            icon = "fas fa-external-link-alt",
            className = "help-detach-icon"
        ) {
            title = "Open in separate window"
            onClick {
                onDetach()
                detachToWindow("$helpTypeLabel \u2014 $viewLabel", rawContent)
            }
        }
        div(content = htmlContent, rich = true)
    }
}

/**
 * Adds a floating help button (FAB) that discovers available help docs via RPC and presents them
 * in an offcanvas panel. Supports single-doc mode and multi-doc tabbed mode, with lazy loading
 * and detach-to-popup-window functionality.
 *
 * @param viewClassName The simple class name of the view, used to look up help documents.
 * @param viewLabel The display label of the view, shown in the offcanvas header.
 */
fun Container.helpButtons(viewClassName: String, viewLabel: String) {
    injectHelpButtonsCss()
    val service = HelpDocsService()
    val rawHtmlCache = mutableMapOf<HelpType, String>()
    val caption = "Ayuda \u2014 $viewLabel"

    val oc = offcanvas(
        caption = caption,
        placement = OffPlacement.END,
        scrollableBody = true,
        closeButton = true,
        backdrop = false,
        className = "help-offcanvas",
    )

    fun isOcVisible() = oc.getElement()?.classList?.contains("show") == true

    fun toggleOc() {
        if (isOcVisible()) oc.hide() else oc.show()
    }

    // FAB button - hidden until we know help is available
    val fab = button(
        text = "",
        icon = "fas fa-question-circle",
        style = ButtonStyle.PRIMARY,
        className = "help-fab"
    ) {
        visible = false
        title = caption
    }

    // Close offcanvas when clicking outside of it and the FAB
    val outsideClickHandler: (Event) -> Unit = { e ->
        if (isOcVisible()) {
            val target = e.target as? org.w3c.dom.Element
            val ocEl = oc.getElement()
            val fabEl = fab.getElement()
            if (target != null && ocEl?.contains(target) != true && fabEl?.contains(target) != true) {
                oc.hide()
            }
        }
    }
    document.addEventListener("click", outsideClickHandler)

    // Discover available help and build content
    KVScope.launch {
        val types = service.getAvailableHelp(viewClassName)
        if (types.isEmpty()) return@launch

        fab.visible = true

        if (types.size == 1) {
            val helpType = types.first()
            fab.icon = helpTypeIcon(helpType)

            val html = ObservableValue("")
            oc.bind(html) { content ->
                if (content.isNotEmpty()) {
                    helpContentWithDetach(
                        helpType.label, viewLabel, content,
                        rawHtmlCache[helpType] ?: content
                    ) { oc.hide() }
                }
            }

            fab.onClick {
                if (html.value.isEmpty()) {
                    KVScope.launch {
                        val rawHtml = service.getHelpContent(viewClassName, helpType)
                        if (rawHtml.isNotEmpty()) {
                            rawHtmlCache[helpType] = rawHtml
                            html.value = extractHtmlContent(rawHtml)
                        }
                    }
                }
                toggleOc()
            }
        } else {
            // Multiple help types: use tabs
            val htmlMap = mutableMapOf<HelpType, ObservableValue<String>>()
            types.forEach { htmlMap[it] = ObservableValue("") }

            oc.add(tabPanel {
                types.forEach { helpType ->
                    val html = htmlMap[helpType]!!
                    tab(label = helpType.label, icon = helpTypeIcon(helpType)) {
                        bind(html) { content ->
                            if (content.isNotEmpty()) {
                                helpContentWithDetach(
                                    helpType.label, viewLabel, content,
                                    rawHtmlCache[helpType] ?: content
                                ) { oc.hide() }
                            }
                        }
                    }
                }
            })

            fab.onClick {
                // Lazy-load all help content
                types.forEach { helpType ->
                    val html = htmlMap[helpType]!!
                    if (html.value.isEmpty()) {
                        KVScope.launch {
                            val rawHtml = service.getHelpContent(viewClassName, helpType)
                            if (rawHtml.isNotEmpty()) {
                                rawHtmlCache[helpType] = rawHtml
                                html.value = extractHtmlContent(rawHtml)
                            }
                        }
                    }
                }
                toggleOc()
            }
        }
    }
}
