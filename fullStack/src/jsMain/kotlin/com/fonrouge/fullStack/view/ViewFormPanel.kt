package com.fonrouge.fullStack.view

import com.fonrouge.base.model.BaseDoc
import io.kvision.core.onChange
import io.kvision.form.FormControl
import io.kvision.form.FormType
import io.kvision.form.GenericFormComponent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty1

class ViewFormPanel<K : BaseDoc<*>>(
    serializer: KSerializer<K>,
    val viewItem: ViewItem<*, K, *, *>,
) : XFormPanel<K>(
    serializer = serializer
) {
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
}
