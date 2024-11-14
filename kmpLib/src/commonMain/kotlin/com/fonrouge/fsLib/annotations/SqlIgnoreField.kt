@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

/**
 * Annotation to indicate that a specific property should be ignored in SQL operations.
 *
 * This annotation can be applied to properties within a class to exclude them from
 * being considered in SQL queries and database operations, such as when performing
 * serialization, deserialization, or other ORM-related activities.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class SqlIgnoreField
