package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.form.FormPanel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.reflect.KProperty1

/**
 * Retrieves the value of a property from the form panel's fields and decodes it using JSON deserialization.
 *
 * @param property The property of the base document type [T] whose value is to be retrieved.
 * @return The decoded value of the property if present, or null if the property field is not found or cannot be decoded.
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
inline fun <T : BaseDoc<*>, reified V : Any> FormPanel<T>.getFormControlValue(property: KProperty1<in T, V?>): V? =
    form.fields[property.name]?.getValue()?.let {
        Json.decodeFromDynamic(it)
    }
