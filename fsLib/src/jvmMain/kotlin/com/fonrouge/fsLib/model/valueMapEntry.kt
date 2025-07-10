package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KProperty1

/**
 * Creates a map representation of a property and its corresponding value.
 *
 * @param T The type of the parent object that the property belongs to.
 * @param V The type of the property's value.
 * @param property The property whose name and value are to be mapped.
 * @param value The value of the property to be included in the map. If the value is of type OffsetDateTime, it will
 *              be serialized using FSOffsetDateTimeSerializer. Otherwise, it will be serialized into a JSON string.
 * @return A map where the key is the name of the property and the value is the serialized representation of the given value.
 */
@Suppress("unused")
inline fun <T : BaseDoc<*>, @OnlyInputTypes reified V> valueMapEntry(
    property: KProperty1<in T, V?>,
    value: V?,
): Map<String, String?> = mapOf(
    property.name to when (value) {
//        is OffsetDateTime -> value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        is OffsetDateTime -> Json.encodeToString(FSOffsetDateTimeSerializer, value)
        else -> Json.encodeToString(value)
    }
)
