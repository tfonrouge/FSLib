package com.fonrouge.fsLib

import org.bson.types.ObjectId

actual fun objectIdHexString(): String {
    return ObjectId().toHexString()
}
