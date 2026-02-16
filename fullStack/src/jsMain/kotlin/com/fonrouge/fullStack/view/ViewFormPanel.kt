@file:Suppress("unused")

package com.fonrouge.fullStack.view

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.view.ViewFormPanel.Companion.xcreate
import com.fonrouge.fullStack.view.ViewItem.TabulatorItem
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

/**
 * Represents a panel for managing form views, including field bindings, custom value serialization,
 * and data processing.
 *
 * @param T The type of the underlying data model associated with the form.
 *
 * Fields:
 * - `serializer`: Used for serializing and deserializing form data values.
 * - `viewItem`: Provides a representation of the current form item for visualization or interaction.
 * - `getModel`: Retrieves the associated data model for the form.
 * - `customBindings`: Stores custom value-to-form mappings.
 * - `serializedValueMap`: Tracks serialized values of the form controls.
 *
 * This class provides advanced functionality for binding form fields to data model properties,
 * managing custom mappings, performing validations, and constructing data models based on user input.
 */
open class ViewFormPanel<T : BaseDoc<*>>(
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
        ): ViewFormPanel<K> {
            val formPanel =
                ViewFormPanel(
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
     * A mutable map that holds custom bindings, mapping string keys to `CustomMapValue` instances.
     *
     * This map serves as a container for storing key-value pairs where the keys are of type `String`
     * and the values are of type `CustomMapValue<*, *>`. The purpose of these mappings is to associate
     * unique identifiers (keys) with corresponding custom data (values).
     */
    val customBindings = mutableMapOf<String, CustomMapValue<*, *>>()

    /**
     * A mutable map that holds serialized key-value pairs where the key is a non-null string
     * and the value is a nullable string. This map can be used to store and retrieve data
     * in a serialized format for various operations such as persistence or transmission.
     */
    var serializedValueMap: MutableMap<String, String?> = mutableMapOf()

    /**
     * A generic data class designed to associate a form control with a serializer and transformation
     * functions for converting between a domain-specific value type and its corresponding representation
     * as a string.
     *
     * @param F The type of the form control, which must extend the `FormControl` class.
     * @param V The domain-specific type of the values being handled.
     * @property formControl The form control instance used for setting and retrieving values.
     * @property serializer The serializer used for converting the domain-specific type `V` into a JSON string and vice versa.
     * @property valueToControl A transformation function that maps a domain-specific value of type `V?` to its string representation for the form control.
     * @property valueFromControl A transformation function that maps a string representation from the form control back to a domain-specific value of type `V?`.
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
     * Adds the given property and its associated value to the `serializedValueMap` after serializing the value.
     *
     * @param property The property whose name will be used as the key in the map.
     * @param value The value associated with the property to be serialized and stored in the map.
     */
    inline fun <reified V> addToSerializedValueMap(property: KProperty1<T, V?>, value: V) {
        serializedValueMap[property.name] = Json.encodeToString<V>(value = value)
    }

    /**
     * Binds a `DateFormControl` to a specified property with optional validation and layout configuration.
     *
     * @param key The property of the model object to bind this control to.
     * @param required Indicates whether the field is mandatory. Default is false.
     * @param requiredMessage The message to display when the field is required but left empty. Default is null.
     * @param layoutType The type of layout for the form control. Default is null.
     * @param validatorMessage A lambda function that provides a custom validation message for the control. Default is null.
     * @param validator A lambda function that performs custom validation logic for the control. Default is null.
     * @return The control instance to allow method chaining.
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
     * Binds a `StringFormControl` to a specified key with optional validation and layout configuration.
     *
     * @param key The key to bind this control to.
     * @param required Indicates whether the field is mandatory. Default is false.
     * @param requiredMessage The message to display when the field is required but left empty. Default is null.
     * @param layoutType The type of layout for the form control. Default is null.
     * @param validatorMessage A lambda function that provides a custom validation message for the control. Default is null.
     * @param validator A lambda function that performs custom validation logic for the control. Default is null.
     * @return The control instance to allow method chaining.
     */
    @Suppress("unused")
    fun <C : StringFormControl> C.bindCustom(
        key: String, required: Boolean = false, requiredMessage: String? = null,
        layoutType: FormType? = null,
        validatorMessage: ((C) -> String?)? = null,
        validator: ((C) -> Boolean?)? = null,
    ): C = bind(key, required, requiredMessage, layoutType, validatorMessage, validator)

    /**
     * Binds a custom value to the form control, allowing serialization and deserialization of the property.
     *
     * @param property The property to bind to the form's control. This represents the data field being bound.
     * @param serializer A `KSerializer` for the type of the property. Defaults to the serializer for the reified type `V?`.
     * @param valueToControl A function that converts the property value into a `String?` to populate the form control. Defaults to JSON serialization of the value.
     * @param valueFromControl A function that converts the `String?` value from the form control into a format suitable for deserialization. Defaults to returning the string as-is
     * .
     * @param required A flag indicating whether the field is required. Defaults to `false`.
     * @param requiredMessage A message to display if the field is required but not provided by the user. Defaults to `null`.
     * @param layoutType The layout type of the form control, if applicable. Defaults to `null`.
     * @param validatorMessage A lambda that provides a custom validation message for the control if the validation fails. Defaults to `null`.
     * @param validator A lambda that provides custom validation logic for the form control. The lambda should return `true` or `false` depending on validation result. Defaults to
     *  `null`.
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
     * Updates the form control values in the associated form based on a provided map of serialized key-value pairs.
     * This method iterates through the `serializedValueMap`, attempts to find matching form fields or custom bindings,
     * and applies the deserialized values to the corresponding form controls.
     *
     * The deserialization process varies based on the type of `formControl`:
     * - For `DateFormControl`, the value is parsed as a `kotlin.js.Date`.
     * - For `KFilesFormControl`, the value is deserialized as a list of `KFile` objects.
     * - For other types, the value is applied directly using the `setValue` method.
     *
     * Keys from the `serializedValueMap` that were successfully assigned are removed from the map
     * to ensure they are not processed again.
     *
     * If a value is null or a matching form control cannot be found, the key is skipped or
     * the form control value is set to null.
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
     * Retrieves the value of a control associated with the specified property.
     *
     * @param property The property whose corresponding control value is to be retrieved.
     * @return The value of the control if it exists, or null if no matching control is found.
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
     * Retrieves a custom value associated with the specified property.
     *
     * @param property The property whose associated custom value is to be retrieved.
     * @return The custom value associated with the property, or null if a type mismatch occurs or the value is null.
     * @throws IllegalArgumentException If the property is not bound with a custom value.
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
     * Retrieves and processes serialized data using custom bindings, tabulator entries,
     * and serialized value maps. Combines these into a final data object that adheres to
     * the generic type T.
     *
     * If no custom bindings, serialized value maps, or tabulator entries are present,
     * this method defaults to the superclass implementation.
     *
     * @return The processed data of type T.
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
     * Sets a custom value for the specified property in the custom bindings map.
     * If a corresponding custom binding exists for the property, the value is updated.
     *
     * @param property The property whose associated custom value is to be set.
     * @param value The new value to be set for the specified property. Can be null.
     */
    @Suppress("unused")
    inline fun <reified V> setCustomValue(property: KProperty1<in T, V>, value: V?) {
        @Suppress("UNCHECKED_CAST")
        (customBindings[property.name] as? CustomMapValue<*, V>)?.setValue(value)
    }

    /**
     * Validates the current form and custom bindings, optionally marking fields with validation errors.
     *
     * The method temporarily updates the form fields with custom bindings, invokes the superclass validation
     * logic, and then removes the temporary custom fields from the form. This ensures that custom bound fields
     * are included in the validation process.
     *
     * @param markFields A boolean indicating whether to mark fields that fail validation. If true, fields with
     * validation errors are visually indicated.
     * @return A boolean indicating the validation result. Returns true if the form passes validation, or false if
     * any validation errors are found.
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
 * Creates a `ViewFormPanel` for the specified data model and configuration.
 *
 * @param K The type of the data model that extends `BaseDoc`.
 * @param method The HTTP method used by the form. Defaults to `null`.
 * @param action The URL to which the form will be submitted. Defaults to `null`.
 * @param enctype The encoding type used by the form. Defaults to `null`.
 * @param type The type of the form (inline, horizontal, or vertical). Defaults to `null`.
 * @param condensed Specifies whether the form should use a condensed layout. Defaults to `false`.
 * @param horizRatio The horizontal ratio for the form fields in a horizontal layout. Defaults to `FormHorizontalRatio.RATIO_2`.
 * @param className Additional CSS classes to apply to the form panel. Defaults to `null`.
 * @param customSerializers A map of custom serializers for specific classes used in the form model. Defaults to `null`.
 * @param viewItem A `ViewItem` object that represents the context for the view. Defaults to `null`.
 * @param getModel A lambda function to provide the model for the form panel. Defaults to a lambda returning the `item` property of `viewItem`.
 * @param init A lambda function for additional initialization logic for the `FormPanel`. Defaults to `null`.
 * @return A `ViewFormPanel` instance initialized with the provided configuration and model.
 */
@Suppress("unused")
inline fun <reified K : BaseDoc<*>> Container.viewFormPanel(
    method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
    type: FormType? = null, condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
    viewItem: ViewItem<*, K, *, *>? = null,
    noinline getModel: (() -> K?) = { viewItem?.item },
    noinline init: (FormPanel<K>.() -> Unit)? = null
): ViewFormPanel<K> {
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
