package com.fonrouge.fslib.lib

import io.kvision.types.toStringF
import kotlin.js.Date

var appDateFormat = "ddd MMM-DD-YYYY hh:mm a"

val Date?.toAppString: String?
    get() {
        return this?.toStringF(appDateFormat)
    }
