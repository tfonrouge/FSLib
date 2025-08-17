@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.base.annotations

/**
 * Annotation to denote that a property serves as a pre-lookup field.
 *
 * This annotation is used to specify properties that play a role in pre-lookup operations,
 * typically for use in scenarios where initial data filtering or validation is required
 * before main database operations. It assists in identifying fields that contribute
 * to lookup logic or validation steps.
 *
 * This annotation is applied at the property level and retained at runtime.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class PreLookupField()
