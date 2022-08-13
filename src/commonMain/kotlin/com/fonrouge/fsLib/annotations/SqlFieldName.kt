@file:Suppress("unused", "RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

@Target(AnnotationTarget.FIELD)
public annotation class SqlField(
    val name: String,
    val length: Int = 255,
    val nullable: Boolean = true,
)
