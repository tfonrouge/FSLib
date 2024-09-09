package com.fonrouge.fsLib.mongoDb.aggregation

import org.bson.BsonDateTime
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.conversions.Bson
import org.litote.kmongo.projection
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import kotlin.reflect.KProperty

/**
 * $dateToString aggregator operator support.
 */
@Suppress("unused")
fun KProperty<TemporalAccessor?>.dateDiff(
    /* TODO: solve to allow an Field reference */endDate: OffsetDateTime,
    unit: DateDiffUnit = DateDiffUnit.second,
    zoneId: ZoneId? = null,
    startOfWeek: String? = null,
): Bson =
    BsonDocument(
        "\$dateDiff",
        BsonDocument().apply {
            set("startDate", BsonString(projection))
            set("endDate", BsonDateTime(endDate.toEpochSecond() * 1000))
            set("unit", BsonString(unit.toString()))
            if (zoneId != null) {
                set("timezone", BsonString(zoneId.id))
            }
            if (startOfWeek != null) {
                set("startOfWeek", BsonString(startOfWeek))
            }
        }
    )

@Suppress("unused")
enum class DateDiffUnit {
    year,
    quarter,
    week,
    month,
    day,
    hour,
    minute,
    second,
    millisecond
}
