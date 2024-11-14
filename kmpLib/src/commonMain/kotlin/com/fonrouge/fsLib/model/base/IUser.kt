package com.fonrouge.fsLib.model.base

/**
 * Interface representing a user in the application.
 *
 * @param UID The type of the user identifier.
 */
interface IUser<UID : Any> : BaseDoc<UID> {
    override val _id: UID
}
