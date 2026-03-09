package com.fonrouge.ssr.layout

import com.fonrouge.ssr.model.FlashMessage
import kotlinx.html.*

/**
 * Defines the HTML page shell that wraps every SSR-rendered page.
 * Override to customize the application chrome (navbar, sidebar, footer, CSS/JS).
 */
abstract class SsrLayout {

    /** Application name displayed in the page title. */
    open val appName: String = "App"

    /**
     * Renders a complete HTML document with the given page title and content.
     *
     * @param pageTitle the specific page title (combined with [appName])
     * @param flashMessages pending flash messages to display
     * @param content the page body content
     */
    fun HTML.page(
        pageTitle: String,
        flashMessages: List<FlashMessage> = emptyList(),
        content: MAIN.() -> Unit,
    ) {
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            title { +"$appName — $pageTitle" }
            link(
                rel = "stylesheet",
                href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css",
            )
            headExtra()
        }
        body {
            bodyWrapper {
                main(classes = "container-fluid mt-3") {
                    renderFlashMessages(flashMessages)
                    content()
                }
            }
            script(src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js") {}
            script(src = "https://cdn.jsdelivr.net/npm/alpinejs@3/dist/cdn.min.js") { defer = true }
            bodyExtra()
        }
    }

    /**
     * Override to wrap the main content area with navigation, sidebar, etc.
     * Default implementation renders content directly in the body.
     */
    open fun BODY.bodyWrapper(content: BODY.() -> Unit) {
        content()
    }

    /** Override to add extra tags to the `<head>` element. */
    open fun HEAD.headExtra() {}

    /** Override to add extra scripts or elements at the end of `<body>`. */
    open fun BODY.bodyExtra() {}

    /**
     * Renders flash messages as Bootstrap alerts with auto-dismiss via Alpine.js.
     */
    private fun MAIN.renderFlashMessages(messages: List<FlashMessage>) {
        if (messages.isEmpty()) return
        messages.forEach { msg ->
            div(classes = "alert ${msg.level.cssClass} alert-dismissible fade show") {
                attributes["role"] = "alert"
                attributes["x-data"] = "{ show: true }"
                attributes["x-show"] = "show"
                attributes["x-init"] = "setTimeout(() => show = false, 5000)"
                +msg.message
                button(type = ButtonType.button, classes = "btn-close") {
                    attributes["x-on:click"] = "show = false"
                }
            }
        }
    }
}

/**
 * Default layout implementation with Bootstrap 5 styling.
 * Provides a minimal page shell suitable for CRUD applications.
 */
class DefaultSsrLayout : SsrLayout()
