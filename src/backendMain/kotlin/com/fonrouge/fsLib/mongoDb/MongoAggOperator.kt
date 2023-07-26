package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.from

@Suppress("unused")
enum class MongoAggOperator {
    substrBytes
    ;

    override fun toString(): String {
        return "\$$name"
    }
}

@Suppress("unused")
infix fun MongoAggOperator.from(expression: Any): Bson = toString().from(expression)
