package com.fonrouge.ssr.model

/**
 * Validation rules that can be applied to form fields.
 * Used by [FormBinder][com.fonrouge.ssr.bind.FormBinder] to validate submitted values.
 */
sealed class FieldValidation {

    /** Field must have a non-blank value. */
    data object Required : FieldValidation()

    /** Field value must be a valid email address. */
    data object Email : FieldValidation()

    /** Field value length must not exceed [max] characters. */
    data class MaxLength(val max: Int) : FieldValidation()

    /** Field value length must be at least [min] characters. */
    data class MinLength(val min: Int) : FieldValidation()

    /** Field value must match the given [regex]. */
    data class Pattern(val regex: String, val message: String) : FieldValidation()

    /** Custom validation function. Returns an error message or null if valid. */
    data class Custom(val name: String, val check: (String?) -> String?) : FieldValidation()
}
