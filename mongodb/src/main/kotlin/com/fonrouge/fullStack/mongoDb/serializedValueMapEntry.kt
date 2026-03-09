package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.serializers.FSOffsetDateTimeSerializer
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import kotlin.reflect.KProperty1

/**
 * Serializes a property and its corresponding value into a map entry where the key is the property name and the value
 * is the serialized string representation of the value. It supports specific serialization formats, such as using a
 * custom serializer for `OffsetDateTime`.
 *
 * @param property The property whose name will serve as the key in the resulting map.
 * @param value The value to be serialized into a string representation and included in the resulting map.
 * @return A map containing a single entry where the key is the property name and the value is the serialized format of the input value.
 */
@Suppress("unused")
inline fun <T : BaseDoc<*>, @OnlyInputTypes reified V> serializedValueMapEntry(
    property: KProperty1<in T, V?>,
    value: V?,
): Map<String, String?> = mapOf(
    property.name to when (value) {
//        is OffsetDateTime -> value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        is OffsetDateTime -> Json.encodeToString(FSOffsetDateTimeSerializer, value)
        else -> Json.encodeToString(value)
    }
)
