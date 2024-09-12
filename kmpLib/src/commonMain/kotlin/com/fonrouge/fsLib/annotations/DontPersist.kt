package com.fonrouge.fsLib.annotations

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@OptionalExpectation
expect annotation class DontPersist()
