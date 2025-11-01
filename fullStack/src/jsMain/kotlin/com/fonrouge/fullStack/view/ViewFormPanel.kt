package com.fonrouge.fullStack.view

import com.fonrouge.base.model.BaseDoc
import io.kvision.core.Widget
import io.kvision.core.onChange
import io.kvision.form.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty1

class ViewFormPanel<K : BaseDoc<*>>(
    serializer: KSerializer<K>,
    val viewItem: ViewItem<*, K, *, *>,
) : FormPanel<K>(
    serializer = serializer
) {
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

        fun getSerializedValue(): String? = getValue()?.let { Json.encodeToString(serializer = serializer, value = it) }
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
        serializer: KSerializer<V?> = serializer<V?>(),
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

        if (this is Widget) {
            if (this is GenericFormComponent<*>) {
                @Suppress("TYPE_INTERSECTION_AS_REIFIED_WARNING")
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
        }

        customMapValues[property.name] = customMapValue
        customMapValue.setValue(viewItem.item?.let { property.get(it) })
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
