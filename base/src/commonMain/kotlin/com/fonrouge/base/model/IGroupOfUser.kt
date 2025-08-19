package com.fonrouge.base.model

import com.fonrouge.base.types.OId

/**
 * Interface representing a group of users.
 *
 * @param T The type parameter extending from `Any`.
 */
interface IGroupOfUser<T : Any> : BaseDoc<OId<T>>, IUserUiParams {
    override val _id: OId<T>
    val description: String
}
