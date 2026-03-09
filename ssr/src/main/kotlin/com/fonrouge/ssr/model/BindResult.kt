package com.fonrouge.ssr.model

/**
 * Result of form parameter binding and validation.
 * Contains either a successfully bound value or a map of field-level errors.
 *
 * @param T the target model type
 */
data class BindResult<T>(
    /** The bound model instance, null if binding/validation failed. */
    val value: T? = null,

    /** Map of field name to list of error messages. Key "_global" for form-level errors. */
    val errors: Map<String, List<String>> = emptyMap(),

    /** Raw form values for re-rendering the form with user input preserved. */
    val rawValues: Map<String, String> = emptyMap(),
) {
    /** Whether the binding produced any errors. */
    val hasErrors: Boolean get() = errors.isNotEmpty()
}
