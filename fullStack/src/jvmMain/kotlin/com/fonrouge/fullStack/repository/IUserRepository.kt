package com.fonrouge.fullStack.repository

import com.fonrouge.base.model.IUser
import com.fonrouge.base.model.UserSession
import io.ktor.server.application.*

/**
 * Backend-agnostic interface for user repository operations required by [IRepository].
 *
 * Implementations provide user lookup and session extraction without coupling to
 * a specific database engine. The MongoDB implementation ([com.fonrouge.fullStack.mongoDb.IUserColl])
 * and potential SQL implementations both fulfill this contract.
 *
 * @param U The user entity type, must implement [IUser].
 * @param UID The user identifier type.
 */
interface IUserRepository<U : IUser<UID>, UID : Any> {

    /**
     * Retrieves a user entity by its identifier.
     *
     * @param id The user identifier. Returns null if the id is null or not found.
     * @return The user entity, or null if not found.
     */
    suspend fun findUserById(id: UID?): U?

    /**
     * Retrieves a user entity from the Ktor application call session.
     *
     * @param call The [ApplicationCall] from which the user session will be retrieved. Can be null.
     * @return The user entity associated with the session, or null if not found.
     */
    suspend fun userFromCall(call: ApplicationCall?): U?

    /**
     * Retrieves the user session from the given [ApplicationCall].
     *
     * @param call The [ApplicationCall] from which the user session will be retrieved. Can be null.
     * @return The [UserSession] associated with the call, or null if unavailable.
     */
    fun userSessionFromCall(call: ApplicationCall?): UserSession<UID>?
}
