package com.fonrouge.ssr.plugin

import com.fonrouge.ssr.PageDef
import com.fonrouge.ssr.auth.AllowAllAuth
import com.fonrouge.ssr.auth.SsrAuth
import com.fonrouge.ssr.layout.DefaultSsrLayout
import com.fonrouge.ssr.layout.SsrLayout
import com.fonrouge.ssr.routing.installCrudRoutes
import com.fonrouge.ssr.session.FlashSession
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

/**
 * Ktor application plugin that installs SSR CRUD support.
 *
 * Registers CRUD routes for all configured [PageDef] instances,
 * sets up flash message sessions, and applies the page layout.
 *
 * Usage:
 * ```kotlin
 * fun Application.module() {
 *     install(FsSsr) {
 *         layout = MyAppLayout()
 *         auth = RepositoryAuth()
 *         page(CustomerPage(customerRepo))
 *         page(ProductPage(productRepo))
 *     }
 * }
 * ```
 */
val FsSsr = createApplicationPlugin("FsSsr", createConfiguration = ::SsrConfig) {
    val config = pluginConfig

    // Install flash message session support
    try {
        application.install(Sessions) {
            cookie<FlashSession>("FSS_FLASH")
        }
    } catch (_: DuplicatePluginException) {
        // Sessions already installed — register the flash session type
        // (the consumer must register FlashSession in their Sessions config)
    }

    application.routing {
        config.pages.forEach { pageDef ->
            @Suppress("UNCHECKED_CAST")
            installCrudRoutes(
                pageDef as PageDef<Nothing, Nothing, Nothing, Nothing>,
                config.layout,
                config.auth,
            )
        }
    }
}

/**
 * Configuration for the [FsSsr] plugin.
 */
class SsrConfig {
    /** The page layout used to wrap all SSR pages. */
    var layout: SsrLayout = DefaultSsrLayout()

    /** The auth strategy for permission checking. */
    var auth: SsrAuth = AllowAllAuth()

    internal val pages = mutableListOf<PageDef<*, *, *, *>>()

    /**
     * Registers a [PageDef] for CRUD route generation.
     */
    fun page(pageDef: PageDef<*, *, *, *>) {
        pages.add(pageDef)
    }
}
