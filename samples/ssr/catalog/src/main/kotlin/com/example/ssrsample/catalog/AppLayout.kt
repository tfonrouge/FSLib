package com.example.ssrsample.catalog

import com.fonrouge.ssr.layout.SsrLayout
import kotlinx.html.*

/**
 * Custom application layout with a Bootstrap navbar.
 * Provides navigation links to Products and Customers pages.
 */
class AppLayout : SsrLayout() {

    override val appName = "SSR Sample"

    override fun BODY.bodyWrapper(content: BODY.() -> Unit) {
        nav(classes = "navbar navbar-expand-lg navbar-dark bg-dark") {
            div(classes = "container-fluid") {
                a(classes = "navbar-brand", href = "/") { text("SSR Sample") }
                button(classes = "navbar-toggler", type = ButtonType.button) {
                    attributes["data-bs-toggle"] = "collapse"
                    attributes["data-bs-target"] = "#navbarNav"
                    span(classes = "navbar-toggler-icon")
                }
                div(classes = "collapse navbar-collapse") {
                    id = "navbarNav"
                    ul(classes = "navbar-nav") {
                        li(classes = "nav-item") {
                            a(classes = "nav-link", href = "/products") { text("Products") }
                        }
                        li(classes = "nav-item") {
                            a(classes = "nav-link", href = "/customers") { text("Customers") }
                        }
                    }
                }
            }
        }
        content()
    }
}
