package com.fonrouge.fsLib.model.base

/**
 * The base class for the items referenced in the frontend-backend transactions
 *
 * If a [Enum] class is used as [_id] [T] type, remember to set the idKClass property on the ConfigViewContainer definition
 * to correctly serialize/deserialize the [_id] when referencing it in the transactions.
 */
interface BaseDoc<T : Any> {
    @Suppress("PropertyName")
    val _id: T?
}
