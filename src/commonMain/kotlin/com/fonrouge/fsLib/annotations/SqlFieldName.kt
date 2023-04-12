@file:Suppress("unused", "RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class SqlField(
    val name: String,
    val primaryKey: Boolean = false,
    val length: Int = 255,
    val nullable: Boolean = true,
)
