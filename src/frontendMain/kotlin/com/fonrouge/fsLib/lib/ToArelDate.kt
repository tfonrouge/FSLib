package com.fonrouge.fsLib.lib

import io.kvision.types.toStringF
import kotlin.js.Date

var defaultDateTimeFormat = "ddd MMM-DD-YYYY hh:mm a"
var defaultDateFormat = "ddd MMM-DD-YYYY"

@Suppress("unused")
val Date?.toDateTimeString: String?
    get() {
        return this?.toStringF(defaultDateTimeFormat)
    }

@Suppress("unused")
val Date?.toDateString: String?
    get() {
        return this?.toStringF(defaultDateFormat)
    }
