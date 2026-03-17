@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.base.annotations

/**
 * Marks a body property as intentionally non-persisted (computed).
 *
 * In fsLib, only primary constructor parameters of [com.fonrouge.base.model.BaseDoc] subclasses
 * are persisted to the database. Properties declared in the class body are automatically stripped
 * before writes. This annotation makes that intent explicit and serves as documentation for
 * developers reading the model.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class Invoice(
 *     override val _id: String,
 *     val total: Double,          // persisted (constructor parameter)
 * ) : BaseDoc<String> {
 *     @Computed
 *     val formattedTotal: String   // NOT persisted (body property)
 *         get() = "$$total"
 * }
 * ```
 *
 * @see com.fonrouge.base.model.BaseDoc
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Computed
