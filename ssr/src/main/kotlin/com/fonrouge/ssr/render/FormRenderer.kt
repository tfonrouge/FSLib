package com.fonrouge.ssr.render

import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.ssr.model.FieldDef
import com.fonrouge.ssr.model.FieldType
import kotlinx.html.*
import kotlin.reflect.full.memberProperties

/**
 * Renders a single form field to HTML based on its [FieldDef] configuration.
 * Handles all supported [FieldType]s, error states, and read-only rendering.
 *
 * @param field the field definition
 * @param item the current model instance (null for create forms)
 * @param crudTask the current CRUD operation
 * @param errors list of validation errors for this field
 * @param rawValues raw form values from a previous submission (for preserving user input)
 */
fun <T : BaseDoc<*>> FlowContent.renderField(
    field: FieldDef<T>,
    item: T?,
    crudTask: CrudTask,
    errors: List<String>?,
    rawValues: Map<String, String> = emptyMap(),
) {
    val hasError = !errors.isNullOrEmpty()
    val isReadOnly = crudTask == CrudTask.Read || field.readOnly
    val value = rawValues[field.name]
        ?: item?.let { getPropertyValue(it, field.propertyName) }?.toString()
        ?: ""

    // Custom render override
    field.customRender?.let { customFn ->
        div { unsafe { +customFn(item, errors ?: emptyList()) } }
        return
    }

    div(classes = "mb-3") {
        // Label (skip for hidden and checkbox)
        if (field.type != FieldType.Hidden && field.type != FieldType.Checkbox) {
            label(classes = "form-label") {
                htmlFor = "field-${field.name}"
                +field.label
                if (field.required) {
                    span(classes = "text-danger") { +" *" }
                }
            }
        }

        when (field.type) {
            FieldType.Text, FieldType.Email, FieldType.Password,
            FieldType.Number, FieldType.Date, FieldType.DateTime,
            -> {
                val inputType = field.type.toInputType() ?: InputType.text
                input(
                    type = inputType,
                    name = field.name,
                    classes = "form-control${if (hasError) " is-invalid" else ""}",
                ) {
                    id = "field-${field.name}"
                    this.value = value
                    if (isReadOnly) readonly = true
                    if (field.required) required = true
                    field.maxLength?.let { maxLength = it.toString() }
                    field.placeholder?.let { placeholder = it }
                }
            }

            FieldType.TextArea -> {
                textArea(classes = "form-control${if (hasError) " is-invalid" else ""}") {
                    id = "field-${field.name}"
                    name = field.name
                    rows = field.textareaRows.toString()
                    if (isReadOnly) readonly = true
                    if (field.required) required = true
                    +value
                }
            }

            FieldType.Select -> {
                select(classes = "form-select${if (hasError) " is-invalid" else ""}") {
                    id = "field-${field.name}"
                    name = field.name
                    if (isReadOnly) disabled = true
                    if (field.required) required = true
                    option {
                        this.value = ""
                        +"-- Select --"
                    }
                    field.options.forEach { (optVal, optLabel) ->
                        option {
                            this.value = optVal
                            if (optVal == value) selected = true
                            +optLabel
                        }
                    }
                }
            }

            FieldType.Checkbox -> {
                div(classes = "form-check") {
                    input(
                        type = InputType.checkBox,
                        name = field.name,
                        classes = "form-check-input${if (hasError) " is-invalid" else ""}",
                    ) {
                        id = "field-${field.name}"
                        if (value.equals("true", ignoreCase = true) || value == "on") checked = true
                        if (isReadOnly) disabled = true
                    }
                    label(classes = "form-check-label") {
                        htmlFor = "field-${field.name}"
                        +field.label
                        if (field.required) {
                            span(classes = "text-danger") { +" *" }
                        }
                    }
                }
            }

            FieldType.Hidden -> {
                input(type = InputType.hidden, name = field.name) {
                    id = "field-${field.name}"
                    this.value = value
                }
            }

            FieldType.Custom -> {
                // Already handled above via customRender
            }
        }

        // Validation error feedback
        if (hasError) {
            div(classes = "invalid-feedback") {
                style = "display: block"
                +errors.orEmpty().joinToString(". ")
            }
        }

        // Help text
        field.helpText?.let {
            div(classes = "form-text") { +it }
        }
    }
}

/**
 * Extracts a property value from a model instance by property name using reflection.
 */
internal fun <T : Any> getPropertyValue(instance: T, propertyName: String): Any? {
    return try {
        instance::class.memberProperties
            .find { it.name == propertyName }
            ?.getter
            ?.call(instance)
    } catch (_: Exception) {
        null
    }
}
