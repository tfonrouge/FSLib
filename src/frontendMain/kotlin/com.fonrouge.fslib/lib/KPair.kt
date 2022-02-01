package com.fonrouge.fslib.lib

import kotlin.js.Date
import kotlin.reflect.KProperty1

class KPair<T, V>(
    val kProp: KProperty1<T, V>,
    val value: V,
)

infix fun <T> KProperty1<T, Int?>.with(that: Int?): KPair<T, Int?> = KPair(this, that)
infix fun <T> KProperty1<T, Boolean?>.with(that: Boolean?): KPair<T, Boolean?> = KPair(this, that)
infix fun <T> KProperty1<T, String?>.with(that: String?): KPair<T, String?> = KPair(this, that)
infix fun <T> KProperty1<T, Date?>.with(that: Date?): KPair<T, Date?> = KPair(this, that)
infix fun <T> KProperty1<T, Double?>.with(that: Double?): KPair<T, Double?> = KPair(this, that)
infix fun <T> KProperty1<T, Float?>.with(that: Float?): KPair<T, Float?> = KPair(this, that)
