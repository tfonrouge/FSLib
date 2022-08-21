package com.fonrouge.fsLib

import io.kvision.types.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty1

@Serializable
class KPair<T, V>(
    val kProp: KProperty1<T, V>,
    val value: V,
)

infix fun <T> KProperty1<T, Int?>.with(that: Int?) = KPair(this, that)
infix fun <T> KProperty1<T, Boolean?>.with(that: Boolean?) = KPair(this, that)
infix fun <T> KProperty1<T, String?>.with(that: String?) = KPair(this, that)
infix fun <T> KProperty1<T, LocalDateTime?>.with(that: LocalDateTime?) = KPair(this, that)
infix fun <T> KProperty1<T, Double?>.with(that: Double?) = KPair(this, that)
infix fun <T> KProperty1<T, Float?>.with(that: Float?) = KPair(this, that)
infix fun <T> KProperty1<T, Any>.withAny(that: Any) = KPair(this, that)
