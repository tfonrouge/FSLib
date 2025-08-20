package com.fonrouge.base.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user session in the application.
 *
 * @param UID The type of the user identifier.
 * @property userId The unique identifier of the user associated with the session.
 * @property loginTime The timestamp when the user logged in. Defaults to the current system time.
 * @property inactivityUiSecsToNoRefresh Represents the maximum duration in seconds of user inactivity
 * before the UI ceases to refresh. Nullable and can be used for UI inactivity threshold configuration.
 * @property sessionMaxSecs Specifies the maximum duration of the session in seconds. Nullable and
 * used to define session timeout.
 */
@Serializable
data class UserSession<UID : Any>(
    val userId: UID,
    val loginTime: Instant = Clock.System.now(),
    override val inactivityUiSecsToNoRefresh: Int?,
    override val sessionMaxSecs: Int?,
) : IUserSessionParams
