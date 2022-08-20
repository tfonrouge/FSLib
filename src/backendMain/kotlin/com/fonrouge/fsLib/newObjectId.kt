package com.fonrouge.fsLib

import org.bson.types.ObjectId

@Suppress("unused")
actual fun newObjectId(): String {
    return ObjectId().toHexString()
}
