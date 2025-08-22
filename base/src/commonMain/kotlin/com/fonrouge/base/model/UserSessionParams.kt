package com.fonrouge.base.model

import com.fonrouge.base.annotations.Collection
import com.fonrouge.base.types.StringId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Represents user interface parameters related to session and inactivity configurations with serialization support.
 *
 * This data class extends the [BaseDoc] interface and implements the [IUserSessionParams] interface,
 * providing properties to manage session settings, such as the session's maximum duration and
 * inactivity threshold for UI refresh control.
 *
 * @property _id The unique identifier for the user interface parameters of type [StringId].
 * @property inactivityUiSecsToNoRefresh The duration in seconds of user inactivity before the UI ceases to refresh.
 *                                        Defaults to 60 seconds and is always encoded in serialization.
 * @property sessionMaxSecs The maximum allowed duration for a user session, represented as a [Duration].
 *                              Defaults to 12 hours. It defines the upper limit for how long a session can remain active.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@Collection(configAppCollection)
data class UserSessionParams(
    override val _id: StringId<UserSessionParams> = userSessionParamsId,
    override val inactivityUiSecsToNoRefresh: Int?,
    override val inactivityUiSecsToLogout: Int?,
    override val sessionMaxSecs: Int?,
) : BaseDoc<StringId<UserSessionParams>>, IUserSessionParams {
    companion object {
        val userSessionParamsId = StringId<UserSessionParams>("${UserSessionParams::class.simpleName}")
    }
}
