package com.example.ssrsample.advanced

import com.fonrouge.ssr.layout.SsrLayout
import kotlinx.html.*

/**
 * Custom layout with a dark navbar, sidebar-style navigation,
 * and a footer. Demonstrates advanced [SsrLayout] customization.
 */
class AppLayout : SsrLayout() {

    override val appName = "Project Tracker"

    override fun BODY.bodyWrapper(content: BODY.() -> Unit) {
        nav(classes = "navbar navbar-expand-lg navbar-dark bg-primary") {
            div(classes = "container-fluid") {
                a(classes = "navbar-brand fw-bold", href = "/") { text("Project Tracker") }
                button(classes = "navbar-toggler", type = ButtonType.button) {
                    attributes["data-bs-toggle"] = "collapse"
                    attributes["data-bs-target"] = "#navMain"
                    span(classes = "navbar-toggler-icon")
                }
                div(classes = "collapse navbar-collapse") {
                    id = "navMain"
                    ul(classes = "navbar-nav me-auto") {
                        li(classes = "nav-item") {
                            a(classes = "nav-link", href = "/projects") { text("Projects") }
                        }
                        li(classes = "nav-item") {
                            a(classes = "nav-link", href = "/tasks") { text("Tasks") }
                        }
                    }
                    span(classes = "navbar-text text-light") {
                        text("Advanced SSR Sample")
                    }
                }
            }
        }
        content()
        footer(classes = "bg-light text-center text-muted py-3 mt-4 border-top") {
            small { text("Project Tracker — FSLib SSR Advanced Sample") }
        }
    }

    override fun HEAD.headExtra() {
        style {
            unsafe {
                raw(
                    """
                    body { min-height: 100vh; display: flex; flex-direction: column; }
                    main { flex: 1; }
                    """.trimIndent()
                )
            }
        }
    }
}
