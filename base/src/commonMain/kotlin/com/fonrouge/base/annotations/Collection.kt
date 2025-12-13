@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.base.annotations

/**
 * An annotation used to specify a collection name for a class.
 *
 * @property name The name of the collection.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Collection(val name: String)
