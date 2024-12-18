package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId
import kotlin.js.Date

/**
 * Get a [Date] from [OId] value
 */
val OId<out Any>?.date: Date?
    get() {
        @Suppress("UNUSED_VARIABLE") val hex = this?.id?.substring(0..7)
        val i = js("parseInt(hex, 16) * 1000") as? Int
        return i?.let { Date(i) }
    }
