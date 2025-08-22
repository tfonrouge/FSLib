package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IUser
import com.fonrouge.base.model.UserSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class IUserColl<CCU : ICommonContainer<U, UID, FILT>, U : IUser<UID>, UID : Any, FILT : IApiFilter<*>>(
    commonContainer: CCU,
    debug: Boolean = false
) : Coll<CCU, U, UID, FILT, UID>(
    commonContainer = commonContainer,
    debug = debug,
) {
    private val expireTimeUser = mutableMapOf<UID, Pair<Instant, U?>>()

    override val userCollFun: () -> IUserColl<CCU, U, UID, FILT> = { this }

    /**
     * Retrieves a user entity from the application call session.
     *
     * This method extracts the user session from the provided [ApplicationCall], obtains the user ID from the session,
     * and then fetches the corresponding user entity using the `findById` method. If the call or session is null, or if no
     * user is associated with the session, the method returns null.
     *
     * @param call The [ApplicationCall] from which the user session will be retrieved. Can be null.
     * @return The user entity associated with the user session, or null if no user is found or if the session is invalid.
     */
    suspend fun userFromCall(call: ApplicationCall?): U? = findById(call?.sessions?.get<UserSession<UID>>()?.userId)

    /**
     * Retrieves the user session from the given [ApplicationCall].
     *
     * This function attempts to extract a user session of type [UserSession] from the provided application call,
     * if available. If the call or session is null, it returns null.
     *
     * @param call The [ApplicationCall] from which the user session will be retrieved. Can be null.
     * @return The [UserSession] associated with the call, or null if there is no valid session.
     */
    fun userSessionFromCall(call: ApplicationCall?): UserSession<UID>? = call?.sessions?.get<UserSession<UID>>()

    /**
     * Retrieves a user entity from the application call session with an optional expiration time.
     *
     * This method extracts the user session from the given [ApplicationCall], retrieves the user ID from the session,
     * and uses it to obtain the corresponding user entity. If the session is null, or if no user is associated with
     * the session, the method returns null. The expiration time can be used to regulate cache validity.
     *
     * @param call The [ApplicationCall] from which the user session will be retrieved. Can be null.
     * @param expireTime The expiration time in seconds for checking cached user validity. Defaults to 60 seconds.
     * @return The user entity associated with the session if found and valid, or null if no valid user is found.
     */
    @Suppress("unused")
    suspend fun userWithExpireTime(call: ApplicationCall?, expireTime: Int = 60): U? =
        call?.sessions?.get<UserSession<UID>>()?.userId?.let { userWithExpireTime(it) }

    /**
     * Retrieves a user with an expiration time based on the given user ID.
     *
     * This function checks if a user is already cached with their associated last access time.
     * If the user is not cached, or if the cached user data has expired based on the given expiration time,
     * the user is re-fetched from the database via the `findById` method. The newly fetched user is then updated
     * in the cache with the current timestamp. If the cached user data has not expired, the cached user is returned.
     *
     * @param userId The unique identifier of the user.
     * @param expireTime The expiration time in seconds used to validate cached user data. Defaults to 60 seconds.
     * @return The user entity if found and valid, or null if the user cannot be found or is invalid.
     */
    suspend fun userWithExpireTime(userId: UID, expireTime: Int = 60): U? =
        expireTimeUser[userId]?.let { pair ->
            val now = Clock.System.now()
            if (pair.second == null || now.minus(pair.first).inWholeSeconds > expireTime) {
                findById(userId)?.let { user ->
                    expireTimeUser[userId] = now to user
                    user
                }
            } else pair.second
        }
}
