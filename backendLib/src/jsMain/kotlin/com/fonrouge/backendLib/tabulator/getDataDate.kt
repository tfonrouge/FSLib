package com.fonrouge.backendLib.tabulator

import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.tabulator.js.Tabulator
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty1

/**
 * Retrieves a date-time value from the current cell's data object, based on the provided sequence of Kotlin property references,
 * and deserializes it into an OffsetDateTime object.
 *
 * @param path A vararg of Kotlin property references used to locate the desired date-time value within the cell's data.
 * @return The deserialized OffsetDateTime object, or null if the data does not exist or cannot be deserialized.
 */
@Suppress("unused")
fun Tabulator.CellComponent.getDataDate(vararg path: KProperty1<*, *>): OffsetDateTime? {
    return getDataValue<Any?>(path = path)?.let {
        Json.decodeFromString(
            deserializer = FSOffsetDateTimeSerializer,
            string = JSON.stringify(it)
        )
    }
}
