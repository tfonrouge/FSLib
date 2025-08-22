package com.fonrouge.base.model

import com.fonrouge.base.annotations.Collection
import com.fonrouge.base.types.StringId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Represents session parameters for a user, including inactivity thresholds and session limits.
 *
 * This class extends the base document structure and implements the user session configuration interface.
 * It defines properties to manage session behavior based on user inactivity and overall session duration.
 *
 * @property _id The unique identifier for the session parameters.
 * @property inactivityUiSecsToNoRefresh The maximum inactivity duration in seconds before user interface updates stop.
 * @property inactivityUiSecsToLogout The maximum inactivity duration in seconds before the user is logged out.
 * @property sessionMaxSecs The maximum duration of a session in seconds.
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
