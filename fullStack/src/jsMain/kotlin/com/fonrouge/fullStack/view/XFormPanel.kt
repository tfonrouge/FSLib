@file:Suppress("unused")

package com.fonrouge.fullStack.view

import com.fonrouge.fullStack.view.XFormPanel.Companion.xcreate
import io.kvision.core.Container
import io.kvision.core.onChange
import io.kvision.form.*
import kotlinx.datetime.Instant
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class XFormPanel<K : Any>(
    method: FormMethod? = null,
    action: String? = null,
    enctype: FormEnctype? = null,
    type: FormType? = null,
    condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    serializer: KSerializer<K>? = null,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null
) : FormPanel<K>(
    method = method,
    action = action,
    enctype = enctype,
    type = type,
    condensed = condensed,
    horizRatio = horizRatio,
    className = className,
    serializer = serializer,
    customSerializers = customSerializers
) {
    companion object {

        inline fun <reified K : Any> xcreate(
            method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
            type: FormType? = null, condensed: Boolean = false,
            horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2, className: String? = null,
            customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
            noinline init: (FormPanel<K>.() -> Unit)? = null
        ): XFormPanel<K> {
            val formPanel =
                XFormPanel(
                    method,
                    action,
                    enctype,
                    type,
                    condensed,
                    horizRatio,
                    className,
                    serializer<K>(),
                    customSerializers
                )
            init?.invoke(formPanel)
            return formPanel
        }

    }

    /**
     * Holds a mutable mapping of property references of type [KProperty1] to optional string representations.
     * Used to store custom mappings of data fields to their serialized or stringified values.
     * This can be used in data transformation operations or for managing dynamic configurations.
     */
    val customMapValues = mutableMapOf<String, CustomMapValue<*, *>>()

    /**
     * A data class that represents a custom map structure for managing a form control with associated
     * serialization and transformation logic for its value.
     *
     * @param F The type of the form control.
     * @param V The type of the value associated with the form control.
     * @property formControl The form control instance used to interact with the value.
     * @property serializer The serializer used for encoding and decoding the value.
     * @property valueToControl A transformation function to convert the value of type [V]
     * to a string representation for the form control.
     * @property valueFromControl A transformation function to convert a string representation from
     * the form control back to a value of type [V].
     */
    data class CustomMapValue<F : FormControl, V>(
        val formControl: F,
        val serializer: KSerializer<V?>,
        val valueToControl: ((V?) -> String?),
        val valueFromControl: ((String?) -> String?),
    ) {
        fun setValue(value: V?) {
            val x: String? = value?.let { valueToControl(it) }
            formControl.setValue(x)
        }

        fun getValue(): V? = formControl.getValue()?.toString()?.let { it ->
            valueFromControl(it)?.let {
                Json.decodeFromString(deserializer = serializer, string = it)
            }
        }

        @Suppress("unused")
        fun getSerializedValue(): String? = getValue()?.let { Json.encodeToString(serializer = serializer, value = it) }
    }

    /**
     * Binds the given form control to a property with configurable validation and layout options.
     *
     * @param key The property used to bind the form control, identified by its name.
     * @param required Specifies if the field is mandatory. Default is false.
     * @param requiredMessage The message displayed when the field is required but left empty. Default is null.
     * @param layoutType The desired layout type for the form control. Default is null.
     * @param validatorMessage A lambda function providing a custom validation message for the control. Default is null.
     * @param validator A lambda function implementing custom validation logic for the control. Default is null.
     * @return The form control itself, enabling method chaining.
     */
    @Suppress("unused")
    fun <C : DateFormControl> C.bind(
        key: KProperty1<K, Instant?>, required: Boolean = false, requiredMessage: String? = null,
        layoutType: FormType? = null,
        validatorMessage: ((C) -> String?)? = null,
        validator: ((C) -> Boolean?)? = null
    ): C {
        return bind(key.name, required, requiredMessage, layoutType, validatorMessage, validator)
    }

    /**
     * Binds a custom value to a form control with additional options for validation and layout configuration.
     *
     * @param key The key used to identify the control in the binding process.
     * @param required Indicates whether the field is mandatory. Default is false.
     * @param requiredMessage The message displayed when the field is required but not filled. Default is null.
     * @param layoutType The type of layout used for the control. Default is null.
     * @param validatorMessage A function that provides a custom validation message for the control. Default is null.
     * @param validator A function that performs custom validation logic for the control. Default is null.
     * @return The control itself, allowing for method chaining.
     */
    @Suppress("unused")
    fun <C : StringFormControl> C.bindCustom(
        key: String, required: Boolean = false, requiredMessage: String? = null,
        layoutType: FormType? = null,
        validatorMessage: ((C) -> String?)? = null,
        validator: ((C) -> Boolean?)? = null,
    ): C = bind(key, required, requiredMessage, layoutType, validatorMessage, validator)

    /**
     * Binds a custom value to a form control through serialization and deserialization.
     * This allows a custom transformation between the value stored in the control and
     * its representation in the form's data model.
     *
     * @param property The property of the model object to bind this control to.
     * @param serializer The serializer to be used for serializing and deserializing the bound value.
     *                   Defaults to the serializer for the value's type.
     * @param valueToControl A function to transform the model's value to a string representation for the control.
     *                       Defaults to encoding the value with the provided serializer.
     * @param valueFromControl A function to transform the control's string value back into the model's value.
     *                         Defaults to returning the string as-is.
     */
    @Suppress("unused")
    @OptIn(InternalSerializationApi::class)
    inline fun <F : FormControl, reified V> F.bindCustomValue(
        property: KProperty1<in K, V?>,
        serializer: KSerializer<V?> = serializer(),
        getValue: () -> V?,
        noinline valueToControl: ((V?) -> String?) = { v: V? -> Json.encodeToString(serializer, v) },
        noinline valueFromControl: ((String?) -> String?) = { s: String? -> s },
        required: Boolean = false, requiredMessage: String? = null,
        layoutType: FormType? = null,
        noinline validatorMessage: ((F) -> String?)? = null,
        noinline validator: ((F) -> Boolean?)? = null,
    ) {
        val customMapValue = CustomMapValue<F, V>(
            formControl = this,
            serializer = serializer,
            valueToControl = valueToControl,
            valueFromControl = valueFromControl,
        )
        if (this is GenericFormComponent<*>) {
            onChange {
                (value as? String?)?.let { it ->
                    valueFromControl(it)?.let {
                        Json.decodeFromString(serializer, it)
                    }
                }?.let {
                    customMapValue.setValue(it)
                }
            }
        }
        customMapValues[property.name] = customMapValue
//        customMapValue.setValue(viewItem.item?.let { property.get(it) })
        val v = getValue()
        customMapValue.setValue(v)
        bind(
            key = property.name,
            required = required,
            requiredMessage = requiredMessage,
            layoutType = layoutType,
            validatorMessage = validatorMessage,
            validator = validator
        )
        form.fields.remove(property.name)
    }

    /**
     * Retrieves the value of a form control associated with the specified property.
     * The value is first searched in the custom map values; if not found, it falls back to the form's fields.
     *
     * @param property The property of the model object for which the control value should be retrieved.
     * @return The retrieved value of the form control, or null if no value is found.
     */
    @Suppress("unused")
    fun getControlValue(property: KProperty1<in K, *>): Any? {
        val c = customMapValues[property.name]
        return if (c != null) {
            c.formControl.getValue()
        } else {
            form.fields[property.name]?.getValue()
        }
    }

    /**
     * Retrieves a custom value of type [V] for a specified property from the custom map values.
     *
     * This function attempts to find the serialized value in the `customMapValues` using the property's name
     * as the key. If a serialized value is found, it is deserialized into the specified type [V].
     *
     * Throws an error if the property is not managed as a custom mapped value with [bindCustomValue]
     *
     * @param property the property for which to retrieve the custom value
     * @return the custom value of type [V] if present and successfully deserialized, or null otherwise
     */
    @Suppress("unused")
    inline fun <reified V> getCustomValue(property: KProperty1<in K, V?>): V? =
        customMapValues[property.name]!!.getValue() as? V

    /**
     * Sets a custom value for a specified property in the custom map values.
     *
     * This method updates the value associated with a property in the custom map values by applying
     * the provided transformation logic, if applicable, and pushing the resulting value to the associated
     * form control.
     *
     * @param property The property for which the custom value is being set.
     * @param value The value to be set for the specified property. Can be null.
     */
    @Suppress("unused")
    inline fun <reified V> setCustomValue(property: KProperty1<in K, V>, value: V?) {
        @Suppress("UNCHECKED_CAST")
        (customMapValues[property.name] as? CustomMapValue<*, V>)?.setValue(value)
    }

    /**
     * Validates the form controls within the panel, ensuring they adhere to their respective validation requirements.
     * Updates the form's field mapping with custom values before validation and restores it afterward.
     *
     * @param markFields A boolean indicating whether the invalid fields should be visually marked during validation.
     * @return A boolean result of the validation process, where true indicates successful validation and false indicates failure.
     */
    override fun validate(markFields: Boolean): Boolean {
        customMapValues.forEach { (name, customMapValue) ->
            form.fields[name] = customMapValue.formControl
        }
        return singleRender {
            val result = super.validate(markFields)
            customMapValues.forEach { (name, _) -> form.fields.remove(name) }
            result
        }
    }
}

/**
 * Creates and adds a new `XFormPanel` to the container.
 *
 * @param K The generic type for the form data model.
 * @param method The HTTP method to be used with the form (e.g., GET, POST). Optional.
 * @param action The URL to which the form will submit data. Optional.
 * @param enctype The encoding type of the form, such as `application/x-www-form-urlencoded` or `multipart/form-data`. Optional.
 * @param type The type of the form, determining its visual styling and behavior. Optional.
 * @param condensed Whether the form should use a condensed layout. Default is `false`.
 * @param horizRatio The horizontal ratio used for the label-to-field layout in the form. Default is `FormHorizontalRatio.RATIO_2`.
 * @param className An optional CSS class name to apply to the form. Optional.
 * @param customSerializers A mapping of `KClass` to `KSerializer` to provide custom serializers for form data. Optional.
 * @param init An optional initializer block to configure the form panel after its creation.
 * @return The created `XFormPanel` instance.
 */
@Suppress("unused")
inline fun <reified K : Any> Container.xFormPanel(
    method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
    type: FormType? = null, condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
    noinline init: (FormPanel<K>.() -> Unit)? = null
): XFormPanel<K> {
    val formPanel =
        xcreate<K>(
            method,
            action,
            enctype,
            type,
            condensed,
            horizRatio,
            className,
            customSerializers
        )
    init?.invoke(formPanel)
    this.add(formPanel)
    return formPanel
}

/**
 * Creates and adds an XFormPanel to the container with the specified configuration.
 *
 * @param method The HTTP method to be used for submitting the form (e.g., GET, POST). Default is null.
 * @param action The URL to which the form is submitted. Default is null.
 * @param enctype The encoding type of the form. Default is null.
 * @param type The form type, defining its appearance. Default is null.
 * @param condensed Whether the form should use a condensed layout. Default is false.
 * @param horizRatio The horizontal ratio for the form layout. Default is FormHorizontalRatio.RATIO_2.
 * @param className Additional CSS class names to style the form. Default is null.
 * @param init An optional initialization block applied to the XFormPanel instance. Default is null.
 * @return The created and configured XFormPanel instance, added to the container.
 */
fun Container.xform(
    method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
    type: FormType? = null, condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    init: (XFormPanel<Map<String, Any?>>.() -> Unit)? = null
): XFormPanel<Map<String, Any?>> {
    val formPanel =
        XFormPanel<Map<String, Any?>>(
            method,
            action,
            enctype,
            type,
            condensed,
            horizRatio,
            className
        )
    init?.invoke(formPanel)
    this.add(formPanel)
    return formPanel
}
