package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.form.FormPanel
import kotlin.reflect.KProperty1

/**
 * Sets the value of a specified property in the form's fields.
 *
 * @param property The property of type [KProperty1] whose value needs to be set.
 * @param value The value to set for the given property. Can be null.
 */
fun <T : BaseDoc<*>> FormPanel<T>.setValue(property: KProperty1<out T, *>, value: Any?) {
    form.fields[property.name]?.setValue(value)
}
