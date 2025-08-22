package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.CommonUserSessionParams
import com.fonrouge.base.model.UserSessionParams
import com.fonrouge.base.types.StringId
import kotlin.time.Duration

/**
 * Abstract class representing a specialized collection for managing user session parameters.
 *
 * @constructor Initializes the collection with an optional debug mode.
 * @property debug Indicates whether the collection operates in debug mode.
 *
 * This class extends the generic collection `Coll` and includes behavior specific
 * to managing instances of [UserSessionParams], including retrieving a predefined
 * session parameter and ensuring a default document exists upon initialization.
 */
@Suppress("unused")
abstract class IUserSessionParamsColl<UID : Any>(
    debug: Boolean = false
) : Coll<CommonUserSessionParams, UserSessionParams, StringId<UserSessionParams>, ApiFilter, UID>(
    commonContainer = CommonUserSessionParams,
    debug = debug
) {
    /**
     * Retrieves the user session parameters from the collection using a predefined identifier.
     *
     * @return The retrieved [UserSessionParams] instance if found, or null if it does not exist.
     */
    suspend fun get(): UserSessionParams? {
        return findById(UserSessionParams.userSessionParamsId)
    }

    /**
     * Invoked after the database collection is opened to ensure that the collection
     * contains at least one default document of type [UserSessionParams].
     *
     * If the collection is empty (i.e., the document count is zero), a new instance of
     * [UserSessionParams] is inserted as the default document.
     *
     * This ensures that there is always a predefined session parameter configuration
     * present in the collection upon initialization.
     */
    override suspend fun onAfterOpen() {
        if (coroutine.countDocuments() == 0L) coroutine.insertOne(
            UserSessionParams(
                inactivityUiSecsToNoRefresh = Duration.parse("60s").inWholeSeconds.toInt(),
                inactivityUiSecsToLogout = Duration.parse("30m").inWholeSeconds.toInt(),
                sessionMaxSecs = Duration.parse("12h").inWholeSeconds.toInt(),
            )
        )
    }
}
