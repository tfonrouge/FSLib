@file:Suppress("unused")

package com.fonrouge.fullStack.view

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.view.ViewItem.TabulatorItem
import com.fonrouge.fullStack.view.XFormPanel.Companion.xcreate
import io.kvision.core.Container
import io.kvision.core.onChange
import io.kvision.form.*
import io.kvision.types.KFile
import io.kvision.types.toStringF
import io.kvision.utils.Serialization
import io.kvision.utils.Serialization.toObj
import js.date.Date
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlinx.serialization.serializer
import kotlin.collections.set
import kotlin.js.json
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class XFormPanel<T : BaseDoc<*>>(
    method: FormMethod? = null,
    action: String? = null,
    enctype: FormEnctype? = null,
    type: FormType? = null,
    condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    val serializer: KSerializer<T>,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
    val viewItem: ViewItem<*, T, *, *>? = null,
    val getModel: (() -> T?) = { viewItem?.item }
) : FormPanel<T>(
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
        inline fun <reified K : BaseDoc<*>> xcreate(
            method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
            type: FormType? = null, condensed: Boolean = false,
            horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2, className: String? = null,
            customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
            viewItem: ViewItem<*, K, *, *>? = null,
            noinline getModel: (() -> K?) = { viewItem?.item },
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
                    customSerializers,
                    viewItem,
                    getModel
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
    val customBindings = mutableMapOf<String, CustomMapValue<*, *>>()

    /**
     * A mutable map used for storing key-value pairs where each key is a string, and the value can either be a string or null.
     *
     * This map is primarily utilized for managing the dynamic association between property names and their corresponding values
     * within a form panel. The values in this map are typically serialized representations of the actual data model values.
     * It serves as a central data store for the form controls to retrieve or update their values during form interactions,
     * binding, or validation processes.
     */
    var serializedValueMap: MutableMap<String, String?> = mutableMapOf()

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

        fun getValue(): V? = formControl.getValue()
            ?.toString()
            ?.let(valueFromControl)
            ?.let { Json.decodeFromString(deserializer = serializer, string = it) }

        @Suppress("unused")
        fun getSerializedValue(): String? = getValue()?.let { Json.encodeToString(serializer = serializer, value = it) }
    }

    /**
     * Adds a key-value pair to the `valueMap` by serializing the provided value.
     *
     * This function takes a property and its corresponding value, serializes the value into a JSON string,
     * and stores it in the `valueMap` using the property's name as the key.
     *
     * @param property The property whose name will be used as the key in the `valueMap`.
     * @param value The value to associate with the property; it will be serialized before storage.
     */
    inline fun <reified V> addToValueMap(property: KProperty1<T, V?>, value: V) {
        serializedValueMap[property.name] = Json.encodeToString<V>(value = value)
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
        key: KProperty1<T, Instant?>, required: Boolean = false, requiredMessage: String? = null,
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
        property: KProperty1<in T, V?>,
        serializer: KSerializer<V?> = serializer(),
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
        customBindings[property.name] = customMapValue
        customMapValue.setValue(getModel.invoke()?.let { property.get(it) })
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
     * Populates form controls in a form panel with values from a `valueMap`.
     *
     * This method iterates through the key-value pairs in `valueMap` and associates the values with the
     * appropriate form controls within the `formPanel`. The association is determined by matching the
     * keys of `valueMap` with the form fields or custom map values in the `formPanel`. Once a key-value
     * pair is processed, the key is added to a set of assigned values.
     *
     * If a matching form control is found:
     * - For `DateFormControl`, the value is parsed as a date and set.
     * - For `KFilesFormControl`, the value is decoded into a list of `KFile` objects and set.
     * - For other form control types, the appropriate `setValue` method is used to assign the value.
     * - If the value is null, the `setValue` method is called with `null`.
     *
     * After processing, unprocessed keys are retained in `valueMap`.
     */
    fun formSetDataWithValueMap() {
        val assignedValues = mutableSetOf<String>()
        serializedValueMap.forEach { (key, value) ->
            val formControl = form.fields[key] ?: customBindings[key]?.formControl ?: return@forEach
            assignedValues += key
            value?.let { value -> Json.decodeFromString(JsonElement.serializer(), value) }?.let { value ->
                when (formControl) {
                    is DateFormControl -> formControl.value =
                        Date(value.unsafeCast<String>()).unsafeCast<kotlin.js.Date>()

                    is KFilesFormControl -> formControl.value = Serialization.plain.decodeFromString(
                        ListSerializer(KFile.serializer()),
                        JSON.stringify(value)
                    )

                    else -> formControl.setValue(value)
                }
            } ?: formControl.setValue(null)
        }
        serializedValueMap = serializedValueMap.filterKeys { it !in assignedValues }.toMutableMap()
    }

    /**
     * Retrieves the value of a form control associated with the specified property.
     * The value is first searched in the custom map values; if not found, it falls back to the form's fields.
     *
     * @param property The property of the model object for which the control value should be retrieved.
     * @return The retrieved value of the form control, or null if no value is found.
     */
    @Suppress("unused")
    fun getControlValue(property: KProperty1<in T, *>): Any? {
        val c = customBindings[property.name]
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
    inline fun <reified V> getCustomValue(property: KProperty1<in T, V?>): V? {
        val binding = customBindings[property.name]
            ?: throw IllegalArgumentException("Property '${property.name}' is not bound with bindCustomValue()")

        return try {
            binding.getValue() as? V
        } catch (e: ClassCastException) {
            console.error("Type mismatch for property '${property.name}'", e)
            null
        }
    }

    /**
     * Processes and retrieves data by combining multiple data sources such as `valueMap`,
     * `customMapValues`, and `viewItem?.tabulators` while performing necessary transformations.
     *
     * This method overrides the base implementation and aggregates data using a combination
     * of serialized and dynamically constructed objects. If all primary data sources
     * (`valueMap`, `customMapValues`, `tabulators`) are empty, it delegates to the super
     * implementation.
     *
     * @return The fully constructed and deserialized data object of type `T`.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun getData(): T {
        if (serializedValueMap.isEmpty() && customBindings.isEmpty() && (viewItem?.tabulators?.isEmpty() ?: true)) {
            return super.getData()
        }
        @Suppress("UnusedVariable")
        val base = if (serializedValueMap.isNotEmpty()) {
            val json = js("{}")
            val fromValueMap = serializedValueMap.map { it -> it.key to it.value?.let { JSON.parse<Any?>(it) } }.toMap()
            val fromFields = fromValueMap + form.fields.entries.associateBy(
                keySelector = { it.key },
                valueTransform = { it.value.getValue() }
            )
            fromFields.forEach { (key, value) ->
                val v = when (value) {
                    is kotlin.js.Date -> {
                        value.toStringF()
                    }

                    is List<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        ((value as? List<KFile>)?.toObj(ListSerializer(KFile.serializer())))
                    }

                    else -> value
                }
                json[key] = v
            }
            json
        } else {
            Json.encodeToDynamic(serializer, super.getData())
        }
        val overlay = json()
        customBindings.forEach { (key: String, mapValue): Map.Entry<String, CustomMapValue<*, *>> ->
            overlay[key] = mapValue.getSerializedValue()?.let { JSON.parse(it) }
        }
        viewItem?.tabulators?.forEach { (key: String, tabulatorItem: TabulatorItem<*>) ->
            overlay[key] = tabulatorItem.toPlainObj()
        }
        val merged = js("Object.assign({}, base, overlay)")
        return Json.decodeFromDynamic(serializer, merged)
    }

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
    inline fun <reified V> setCustomValue(property: KProperty1<in T, V>, value: V?) {
        @Suppress("UNCHECKED_CAST")
        (customBindings[property.name] as? CustomMapValue<*, V>)?.setValue(value)
    }

    /**
     * Validates the form controls within the panel, ensuring they adhere to their respective validation requirements.
     * Updates the form's field mapping with custom values before validation and restores it afterward.
     *
     * @param markFields A boolean indicating whether the invalid fields should be visually marked during validation.
     * @return A boolean result of the validation process, where true indicates successful validation and false indicates failure.
     */
    override fun validate(markFields: Boolean): Boolean {
        customBindings.forEach { (name, customMapValue) ->
            form.fields[name] = customMapValue.formControl
        }
        return singleRender {
            val result = super.validate(markFields)
            customBindings.forEach { (name, _) -> form.fields.remove(name) }
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
inline fun <reified K : BaseDoc<*>> Container.xFormPanel(
    method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
    type: FormType? = null, condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
    viewItem: ViewItem<*, K, *, *>? = null,
    noinline getModel: (() -> K?) = { viewItem?.item },
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
            customSerializers,
            viewItem,
            getModel
        )
    init?.invoke(formPanel)
    this.add(formPanel)
    return formPanel
}
