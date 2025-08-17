package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.api.ApiItem
import com.fonrouge.fsLib.services.getUser

/**
 * Retrieves the current user of type [U] associated with the `ApiItem` instance.
 *
 * This method uses the `ApplicationCall` object associated with the `ApiItem` to
 * fetch the user of the specified type [U] from the session if available.
 *
 * @return The user of type [U] if found in the session, or null if no user is present.
 */
@Suppress("unused")
inline fun <reified U : IUser<*>> ApiItem<*, *, *>.getUser() = this.call?.getUser<U>()
