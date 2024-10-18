package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

expect operator fun <T0 : BaseDoc<*>, T1, T2> KProperty1<T0, T1?>.plus(next: KProperty1<T1, T2?>): KProperty1<T0, T2?>
