package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import kotlin.reflect.KClass

abstract class CommonView<FILT : IApiFilter>(
    val label: String,
    val apiFilterKClass: KClass<FILT>,
) {
    init {
        println("label: ${label}, apiFilterKClass: ${apiFilterKClass.simpleName}, this::class.simpleName: ${this::class}")
    }
}
