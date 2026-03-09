package com.fonrouge.base.lib

import com.fonrouge.base.model.BaseDoc
import io.kvision.form.FormPanel
import kotlin.reflect.KProperty1

/**
 * Sets the value of a specified property in the form's fields.
 *
 * @param property The property of type [KProperty1] whose value needs to be set.
 * @param value The value to set for the given property. Can be null.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> FormPanel<T>.setFormControlValue(property: KProperty1<out T, *>, value: Any?) {
    form.fields[property.name]?.setValue(value)
}
