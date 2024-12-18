package com.fonrouge.fsLib

import com.fonrouge.fsLib.types.OId
import org.bson.types.ObjectId

@Suppress("unused")
fun OId<Any>?.toObjectId(): ObjectId? {
    return this?.id?.let { ObjectId(it) }
}
