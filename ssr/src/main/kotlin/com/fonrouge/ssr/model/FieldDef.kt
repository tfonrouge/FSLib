package com.fonrouge.ssr.model

/**
 * Definition of a form field for server-side form rendering and binding.
 * Describes how a model property maps to an HTML form input.
 *
 * @param T the model type this field belongs to
 */
class FieldDef<T>(
    /** Field identifier, matches the model property name. */
    val name: String,
    /** Display label for the form field. */
    val label: String,
    /** The Kotlin property name on the model class. */
    val propertyName: String,
) {
    /** HTML input type for rendering. */
    var type: FieldType = FieldType.Text

    /** Whether the field is required. */
    var required: Boolean = false

    /** Whether the field is read-only regardless of CRUD task. */
    var readOnly: Boolean = false

    /** Placeholder text for the input. */
    var placeholder: String? = null

    /** Help text displayed below the input. */
    var helpText: String? = null

    /** Maximum character length. */
    var maxLength: Int? = null

    /** Options for Select fields: value to label pairs. */
    var options: List<Pair<String, String>> = emptyList()

    /** Bootstrap grid column width (1-12). */
    var colWidth: Int = 12

    /** Number of rows for TextArea fields. */
    var textareaRows: Int = 3

    /** Validation rules applied during form binding. */
    val validators: MutableList<FieldValidation> = mutableListOf()

    /** Custom render function override. Null uses the default renderer. */
    var customRender: ((item: T?, errors: List<String>) -> String)? = null

    /** Marks the field as required and adds a Required validation. */
    fun required() {
        required = true
        validators.add(FieldValidation.Required)
    }

    /** Sets the field type to Email and adds an Email validation. */
    fun email() {
        type = FieldType.Email
        validators.add(FieldValidation.Email)
    }

    /** Sets maximum length and adds a MaxLength validation. */
    fun maxLength(n: Int) {
        maxLength = n
        validators.add(FieldValidation.MaxLength(n))
    }

    /** Sets minimum length and adds a MinLength validation. */
    fun minLength(n: Int) {
        validators.add(FieldValidation.MinLength(n))
    }

    /** Configures the field as a Select with the given option values. */
    fun select(vararg opts: String) {
        type = FieldType.Select
        options = opts.map { it to it }
    }

    /** Configures the field as a Select with value-label pairs. */
    fun select(opts: List<Pair<String, String>>) {
        type = FieldType.Select
        options = opts
    }

    /** Configures the field as a TextArea with the given number of rows. */
    fun textarea(rows: Int = 3) {
        type = FieldType.TextArea
        textareaRows = rows
    }

    /** Configures the field as a Checkbox. */
    fun checkbox() {
        type = FieldType.Checkbox
    }

    /** Configures the field as a Password input. */
    fun password() {
        type = FieldType.Password
    }

    /** Configures the field as a Date input. */
    fun date() {
        type = FieldType.Date
    }

    /** Configures the field as a Hidden input. */
    fun hidden() {
        type = FieldType.Hidden
    }

    /** Configures the field as a Number input. */
    fun number() {
        type = FieldType.Number
    }

    /** Sets the Bootstrap column width (1-12). */
    fun col(width: Int) {
        colWidth = width
    }

    /** Adds a regex pattern validation with a custom error message. */
    fun pattern(regex: String, message: String) {
        validators.add(FieldValidation.Pattern(regex, message))
    }
}
