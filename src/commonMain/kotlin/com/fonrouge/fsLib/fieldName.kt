package com.fonrouge.fsLib

import kotlin.reflect.KProperty1

@Suppress("unused")
fun fieldName(vararg fields: KProperty1<*,*>) : String {
    var result = ""
    fields.forEach {
        result += if(result.isEmpty()) it.name else ".${it.name}"
    }
    return result
}