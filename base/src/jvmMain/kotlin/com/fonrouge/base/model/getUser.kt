package com.fonrouge.base.model

import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.services.getUserSession

/**
 * Retrieves the user session of type [UID] associated with the current API item's application call.
 *
 * @return The user session of type [UID] if available, or null if no session is associated with the call.
 */
@Suppress("unused")
inline fun <reified UID : Any> ApiItem<*, *, *>.getUserSession(): UserSession<UID>? = this.call?.getUserSession<UID>()
