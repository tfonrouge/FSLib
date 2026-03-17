package com.fonrouge.base.model

/**
 * The base interface for all entities referenced in frontend-backend transactions.
 *
 * ## Constructor-only persistence convention
 *
 * fsLib persists **only primary constructor parameters** to the database. Properties declared
 * in the class body are automatically stripped before writes via reflection-based copying.
 * This ensures that computed/derived properties never leak into storage.
 *
 * ```kotlin
 * @Serializable
 * data class Product(
 *     override val _id: String,   // persisted
 *     val name: String,           // persisted
 *     val price: Double,          // persisted
 * ) : BaseDoc<String> {
 *     @Computed
 *     val displayPrice: String    // NOT persisted (body property)
 *         get() = "$$price"
 * }
 * ```
 *
 * Use the [@Computed][com.fonrouge.base.annotations.Computed] annotation on body properties
 * to make the non-persisted intent explicit.
 *
 * If a [Enum] class is used as [_id] [ID] type, remember to set the idKClass property on the
 * ConfigViewContainer definition to correctly serialize/deserialize the [_id] when referencing
 * it in the transactions.
 *
 * @param ID The type of the unique identifier for this document.
 * @see com.fonrouge.base.annotations.Computed
 */
interface BaseDoc<ID> {
    @Suppress("PropertyName")
    val _id: ID
}
