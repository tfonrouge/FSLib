package com.fonrouge.base.model

/**
 * Interface representing a user in the application.
 *
 * @param UID The type of the user identifier.
 */
interface IUser<UID : Any> : BaseDoc<UID>, IUserSessionParams {
    override val _id: UID
}
