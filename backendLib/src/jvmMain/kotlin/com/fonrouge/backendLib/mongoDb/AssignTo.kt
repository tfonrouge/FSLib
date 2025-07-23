package com.fonrouge.backendLib.mongoDb

import com.fonrouge.backendLib.internal.OnlyInputTypes
import kotlin.reflect.KProperty1

data class AssignTo<T, @OnlyInputTypes V>(
    val kField: KProperty1<T, V?>,
    val value: V?,
)

@Suppress("unused")
infix fun <T, @OnlyInputTypes V> KProperty1<T, V>.assignTo(value: V?) = AssignTo(this, value)
