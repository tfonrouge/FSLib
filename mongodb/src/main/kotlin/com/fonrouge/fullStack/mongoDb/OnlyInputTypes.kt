package com.fonrouge.fullStack.mongoDb

/**
 * Annotation used to constrain type parameters to input types only.
 *
 * Applied to type parameters in generic functions and classes to enforce
 * that the type parameter is used only for input, preventing unintended
 * type widening or variance issues.
 */
@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
internal annotation class OnlyInputTypes
