package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.types.OId

/**
 * Interface representing a group of users.
 *
 * @param T The type parameter extending from `Any`.
 */
interface IGroupOfUser<T : Any> : BaseDoc<OId<T>> {
    override val _id: OId<T>
    val description: String
}
