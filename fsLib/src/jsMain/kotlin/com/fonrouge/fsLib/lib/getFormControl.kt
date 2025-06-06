package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.form.FormControl
import io.kvision.form.FormPanel
import kotlin.reflect.KProperty1

/**
 * Retrieves a form control associated with a given property from the form's fields.
 *
 * @param property The property of the data model associated with the form control to be retrieved.
 * @return The form control corresponding to the specified property, or null if no such control exists.
 */
@Suppress("unused")
fun <T : BaseDoc<*>> FormPanel<T>.getFormControl(property: KProperty1<out T, *>): FormControl? =
    form.fields[property.name]
