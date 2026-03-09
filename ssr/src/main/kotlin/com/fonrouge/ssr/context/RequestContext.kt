package com.fonrouge.ssr.context

import com.fonrouge.base.api.IApiFilter
import io.ktor.server.application.*

/**
 * Wraps the Ktor [ApplicationCall] with SSR-specific context.
 * Provides access to the API filter and convenience methods for route handlers.
 *
 * @param FILT the API filter type for this request
 */
class RequestContext<FILT : IApiFilter<*>>(
    /** The underlying Ktor application call. */
    val call: ApplicationCall,

    /** The API filter for this request, constructed from query parameters or defaults. */
    val apiFilter: FILT,
) {
    companion object {
        /**
         * Creates a [RequestContext] from the current call using the PageDef's
         * common container to instantiate the default API filter.
         */
        fun <FILT : IApiFilter<*>> from(
            call: ApplicationCall,
            apiFilter: FILT,
        ): RequestContext<FILT> = RequestContext(call, apiFilter)
    }
}
