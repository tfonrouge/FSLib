@file:Suppress("unused", "RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@OptionalExpectation
public expect annotation class DontPersist()
