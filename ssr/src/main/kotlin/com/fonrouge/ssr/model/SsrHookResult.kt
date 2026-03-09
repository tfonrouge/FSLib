package com.fonrouge.ssr.model

/**
 * Result type for SSR lifecycle hooks in [PageDef][com.fonrouge.ssr.PageDef].
 * Determines whether the route handler continues rendering or redirects.
 */
sealed class SsrHookResult {

    /** Continue with normal rendering. */
    data object Continue : SsrHookResult()

    /** Redirect to [url] with an optional flash message. */
    data class Redirect(
        val url: String,
        val flash: FlashMessage? = null,
    ) : SsrHookResult()
}
