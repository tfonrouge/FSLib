package com.fonrouge.base.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user session containing details about the user's identification and session settings.
 *
 * @param UID The type of the user identifier.
 * @property userId The unique identifier of the user associated with this session.
 * @property loginTime The timestamp of when the user logged in, with a default value set to the current time.
 * @property inactivityUiSecsToNoRefresh The duration in seconds of inactivity after which the UI stops refreshing.
 * @property inactivityUiSecsToLogout The maximum duration in seconds of inactivity before the user is logged out.
 * @property sessionMaxSecs The maximum allowable duration of the session in seconds.
 */
@Serializable
data class UserSession<UID : Any>(
    val userId: UID,
    val loginTime: Instant = Clock.System.now(),
    override val inactivityUiSecsToNoRefresh: Int?,
    override val inactivityUiSecsToLogout: Int?,
    override val sessionMaxSecs: Int?,
) : IUserSessionParams
