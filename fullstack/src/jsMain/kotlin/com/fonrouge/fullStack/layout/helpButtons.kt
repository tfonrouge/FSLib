package com.fonrouge.fullStack.layout

import com.fonrouge.base.enums.HelpType
import com.fonrouge.fullStack.services.HelpDocsServiceRegistry
import io.kvision.core.*
import io.kvision.dropdown.Direction
import io.kvision.dropdown.Separator
import io.kvision.dropdown.ddLink
import io.kvision.dropdown.dropDown
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.hPanel
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.rem
import io.kvision.utils.vh
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

private val helpStyleRegex = Regex("<style[^>]*>[\\s\\S]*?</style>")
private val helpBodyRegex = Regex("<body[^>]*>([\\s\\S]*)</body>")
private val bodySelectorRegex = Regex("""(?<=^|[},\s])body\s*(?=[{\s,])""")

private var helpButtonsCssInjected = false

/**
 * Injects minimal CSS for the offcanvas panel and help content.
 * The "?" button and dropdown use KVision inline styles.
 */
private fun injectHelpButtonsCss() {
    if (helpButtonsCssInjected) return
    helpButtonsCssInjected = true
    val style = document.createElement("style")
    style.textContent = """
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
    HelpType.MANUAL -> "fas fa-book-open"
}

/**
 * Creates a blob URL from raw HTML content for use in iframes.
 *
 * @param rawHtml The complete HTML document string.
 * @return A blob URL pointing to the HTML content.
 */
private fun createBlobUrl(rawHtml: String): String {
    val blob = Blob(arrayOf(rawHtml), BlobPropertyBag(type = "text/html"))
    return URL.createObjectURL(blob)
}

/**
 * Opens the module manual in a modal dialog with an iframe.
 *
 * The modal provides an embedded view of the full manual (with its sidebar layout)
 * and a button to detach it into a separate browser window if needed.
 *
 * @param title The modal caption / window title.
 * @param rawHtml The complete HTML document to display.
 */
private fun showManualModal(title: String, rawHtml: String) {
    val blobUrl = createBlobUrl(rawHtml)
    var modal: Modal? = null
    modal = Modal(
        caption = title,
        size = ModalSize.XLARGE,
    ) {
        hPanel(spacing = 10, alignItems = AlignItems.CENTER) {
            marginBottom = 8.px
            paddingBottom = 8.px
            borderBottom = Border(1.px, BorderStyle.SOLID, Color("#2a2f5f"))
            span("Navega el manual aqu\u00ed o \u00e1brelo en una ventana separada.") {
                color = Color("#9da4d1")
                fontSize = 0.85.rem
            }
            button(
                text = "Ventana separada",
                icon = "fas fa-external-link-alt",
                style = ButtonStyle.OUTLINEINFO,
            ) {
                onClick {
                    window.open(
                        blobUrl,
                        "_blank",
                        "width=1100,height=850,scrollbars=yes,resizable=yes"
                    )
                    modal?.hide()
                }
            }
        }
        tag(TAG.IFRAME) {
            setAttribute("src", blobUrl)
            setAttribute("frameborder", "0")
            width = 100.perc
            height = 72.vh
        }
    }
    // Clean up after modal is hidden: remove DOM manually
    // (KVision's dispose() corrupts sibling widget trees)
    modal.addAfterInsertHook {
        modal?.getElement()?.addEventListener("hidden.bs.modal", {
            modal?.getElement()?.asDynamic()?.remove()
            document.querySelectorAll(".modal-backdrop").asList().forEach { it.asDynamic().remove() }
            document.body?.classList?.remove("modal-open")
            document.body?.style?.removeProperty("overflow")
            document.body?.style?.removeProperty("padding-right")
        })
    }
    modal.show()
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
 * Adds a subtle "?" help button (fixed, bottom-right) that uses a KVision [DropDown]
 * triggered on hover. The dropdown menu shows available help options:
 *
 * - **Manual del M\u00f3dulo** (if available): opens a modal with the full manual in an iframe.
 * - **Ayuda de esta Vista** (if tutorial/context available): opens an offcanvas panel with
 *   tabbed tutorial and context help content.
 *
 * @param viewClassName The simple class name of the view, used to look up help documents.
 * @param viewLabel The display label of the view, shown in the offcanvas header.
 * @param moduleSlug Optional module slug for module-scoped help file lookup
 *                   (e.g., `"importaciones"` resolves to `help-docs/importaciones/{viewClassName}/`).
 */
fun Container.helpButtons(viewClassName: String, viewLabel: String, moduleSlug: String? = null) {
    injectHelpButtonsCss()
    val service = HelpDocsServiceRegistry.service ?: return
    val rawHtmlCache = mutableMapOf<HelpType, String>()
    val caption = "Ayuda \u2014 $viewLabel"

    // Offcanvas for tutorial/context help
    val oc = offcanvas(
        caption = caption,
        placement = OffPlacement.END,
        scrollableBody = true,
        closeButton = true,
        backdrop = false,
        className = "help-offcanvas",
    )

    fun isOcVisible() = oc.getElement()?.classList?.contains("show") == true

    // DropDown "?" button — hidden until help is discovered
    val dd = dropDown(
        text = "?",
        style = ButtonStyle.OUTLINESECONDARY,
        direction = Direction.DROPUP,
        arrowVisible = false,
    ) {
        visible = false
        position = Position.FIXED
        bottom = 20.px
        right = 20.px
        zIndex = 1050
        // Eliminate gap between button and menu so mouseleave doesn't fire in between
        menu.marginBottom = 0.px
        menu.paddingBottom = 2.px
        // Show dropdown on hover, close on leave
        // Check button's aria-expanded (not container's class — Bootstrap puts "show" on menu, not container)
        onEvent {
            mouseenter = {
                if (button.getElement()?.getAttribute("aria-expanded") != "true") toggle()
            }
            mouseleave = {
                if (button.getElement()?.getAttribute("aria-expanded") == "true") toggle()
            }
        }
    }
    // Style the toggle button
    dd.button.apply {
        width = 36.px
        height = 36.px
        borderRadius = CssSize(50, UNIT.perc)
        fontSize = 14.px
        fontWeight = FontWeight.BOLD
        padding = 0.px
    }

    // Close offcanvas when clicking outside
    val outsideClickHandler: (Event) -> Unit = { e ->
        if (isOcVisible()) {
            val target = e.target as? org.w3c.dom.Element
            val ocEl = oc.getElement()
            val ddEl = dd.getElement()
            if (target != null && ocEl?.contains(target) != true && ddEl?.contains(target) != true) {
                oc.hide()
            }
        }
    }
    document.addEventListener("click", outsideClickHandler)

    // Discover available help and build dropdown items + offcanvas content
    KVScope.launch {
        val allTypes = service.getAvailableHelp(viewClassName, moduleSlug)
        if (allTypes.isEmpty()) return@launch

        dd.visible = true

        val hasManual = HelpType.MANUAL in allTypes
        val panelTypes = allTypes - HelpType.MANUAL
        val hasViewHelp = panelTypes.isNotEmpty()

        // Build offcanvas content for tutorial/context
        if (hasViewHelp) {
            if (panelTypes.size == 1) {
                val helpType = panelTypes.first()
                val html = ObservableValue("")
                oc.bind(html) { content ->
                    if (content.isNotEmpty()) {
                        helpContentWithDetach(
                            helpType.label, viewLabel, content,
                            rawHtmlCache[helpType] ?: content
                        ) { oc.hide() }
                    }
                }
                fun lazyLoadSingle() {
                    if (html.value.isEmpty()) {
                        KVScope.launch {
                            val rawHtml = service.getHelpContent(viewClassName, helpType, moduleSlug)
                            if (rawHtml.isNotEmpty()) {
                                rawHtmlCache[helpType] = rawHtml
                                html.value = extractHtmlContent(rawHtml)
                            }
                        }
                    }
                }

                dd.ddLink(
                    label = helpType.label,
                    icon = helpTypeIcon(helpType),
                ) {
                    onClick {
                        dd.toggle()
                        lazyLoadSingle()
                        oc.show()
                    }
                }
            } else {
                val htmlMap = mutableMapOf<HelpType, ObservableValue<String>>()
                panelTypes.forEach { htmlMap[it] = ObservableValue("") }

                oc.add(tabPanel {
                    panelTypes.forEach { helpType ->
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

                fun lazyLoadTabs() {
                    panelTypes.forEach { helpType ->
                        val html = htmlMap[helpType]!!
                        if (html.value.isEmpty()) {
                            KVScope.launch {
                                val rawHtml = service.getHelpContent(viewClassName, helpType, moduleSlug)
                                if (rawHtml.isNotEmpty()) {
                                    rawHtmlCache[helpType] = rawHtml
                                    html.value = extractHtmlContent(rawHtml)
                                }
                            }
                        }
                    }
                }

                dd.ddLink(
                    label = "Ayuda de esta Vista",
                    icon = "fas fa-lightbulb",
                ) {
                    onClick {
                        dd.toggle()
                        lazyLoadTabs()
                        oc.show()
                    }
                }
            }
        }

        // Separator between items
        if (hasManual && hasViewHelp) {
            dd.add(Separator())
        }

        // Manual option — opens modal directly
        if (hasManual) {
            dd.ddLink(
                label = "Manual del M\u00f3dulo",
                icon = "fas fa-book-open",
            ) {
                onClick {
                    dd.toggle()
                    KVScope.launch {
                        val rawHtml = rawHtmlCache[HelpType.MANUAL]
                            ?: service.getHelpContent(viewClassName, HelpType.MANUAL, moduleSlug)
                                .also { if (it.isNotEmpty()) rawHtmlCache[HelpType.MANUAL] = it }
                        if (rawHtml.isNotEmpty()) {
                            showManualModal(
                                "${HelpType.MANUAL.label} \u2014 $viewLabel",
                                rawHtml
                            )
                        }
                    }
                }
            }
        }
    }
}
