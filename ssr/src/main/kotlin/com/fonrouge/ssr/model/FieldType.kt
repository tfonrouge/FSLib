package com.fonrouge.ssr.model

import kotlinx.html.InputType

/**
 * Supported form field types for SSR rendering.
 * Maps to HTML input types and custom rendering strategies.
 */
enum class FieldType {
    Text,
    TextArea,
    Number,
    Email,
    Password,
    Date,
    DateTime,
    Select,
    Checkbox,
    Hidden,
    Custom;

    /**
     * Maps this field type to the corresponding HTML input type.
     * Returns null for types that don't map to `<input>` (TextArea, Select, Custom).
     */
    fun toInputType(): InputType? = when (this) {
        Text -> InputType.text
        Number -> InputType.number
        Email -> InputType.email
        Password -> InputType.password
        Date -> InputType.date
        DateTime -> InputType.dateTimeLocal
        Checkbox -> InputType.checkBox
        Hidden -> InputType.hidden
        TextArea, Select, Custom -> null
    }
}
