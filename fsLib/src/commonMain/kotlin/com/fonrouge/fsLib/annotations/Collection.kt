@file:Suppress("RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

/**
 * An annotation used to specify a collection name for a class.
 *
 * @property name The name of the collection.
 */
@Target(AnnotationTarget.CLASS)
public annotation class Collection(val name: String)
