package com.fonrouge.base.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user session within the application, encapsulating session-related details
 * including the user's identifier, session code, and timestamps.
 *
 * @param UID The type of the user identifier, which must extend from [Any].
 * @property userId The unique identifier of the user associated with this session.
 * @property loginTime The timestamp indicating when the session was created. Defaults to the current system time.
 * @property lastActivity The timestamp indicating the user's most recent activity within the session.
 * @property sessionDuration The total duration of the session, measured in seconds.
 * @property inactivitySecs The limit, in seconds, for user inactivity before the session expires.
 */
@Serializable
data class UserSession<UID : Any>(
    val userId: UID,
    val loginTime: Instant = Clock.System.now(),
    val lastActivity: Instant,
    val sessionDuration: Long,
    val inactivitySecs: Long,
)
