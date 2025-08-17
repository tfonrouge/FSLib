package com.fonrouge.fullStack.mongoDb

import com.fonrouge.fullStack.internal.OnlyInputTypes
import kotlin.reflect.KProperty1

data class AssignTo<T, @OnlyInputTypes V>(
    val kField: KProperty1<in T, V?>,
    val value: V?,
)

@Suppress("unused")
infix fun <T, @OnlyInputTypes V> KProperty1<in T, V?>.assignTo(value: V?) = AssignTo(this, value)
