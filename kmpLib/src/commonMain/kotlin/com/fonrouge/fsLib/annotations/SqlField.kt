@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

/**
 * Annotation to specify details for SQL fields in database operations.
 *
 * @property name The name of the SQL field. Defaults to an empty string.
 * @property compound Indicates if the field is a compound field. Defaults to false.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class SqlField(
    val name: String = "",
    val compound: Boolean = false,
)
