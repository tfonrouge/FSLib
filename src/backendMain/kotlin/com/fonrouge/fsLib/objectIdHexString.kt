package com.fonrouge.fsLib

import org.bson.types.ObjectId

@Suppress("unused")
actual fun objectIdHexString(): String {
    return ObjectId().toHexString()
}
