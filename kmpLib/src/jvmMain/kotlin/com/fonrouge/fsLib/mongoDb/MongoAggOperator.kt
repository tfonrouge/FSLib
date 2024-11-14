package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.from

/**
 * Enum class representing MongoDB aggregation operators.
 *
 * MongoDB aggregation operators are used to transform and analyze data within aggregations.
 */
@Suppress("unused")
enum class MongoAggOperator {
    substrBytes,
    replaceWith,
    ;

    override fun toString(): String {
        return "\$$name"
    }
}

/**
 * Applies the aggregation operator to the given expression.
 *
 * @param expression The expression to be used with the aggregation operator.
 * @return A BSON document representing the aggregation operation.
 */
@Suppress("unused")
infix fun MongoAggOperator.from(expression: Any): Bson = toString().from(expression)
