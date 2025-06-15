package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson
import org.litote.kmongo.from

/**
 * Represents an enumeration of MongoDB aggregation framework operators.
 * Each operator corresponds to a specific functionality used in aggregation pipelines.
 * This enum overrides the `toString` method to return the operator name prefixed with `$`.
 *
 * Note: These operators are not provided by KMongo.
 *
 */
@Suppress("unused")
enum class MongoAggOperator {
    substrBytes,
    replaceWith,
    switch,
    toBool,
    toDate,
    toDecimal,
    toDouble,
    toHashedIndexKey,
    toInt,
    toLong,
    toObjectId,
    toString,
    ;

    override fun toString(): String {
        return "$$name"
    }
}

/**
 * Applies the aggregation operator to the given expression.
 *
 * @param expression The expression to be used with the aggregation operator.
 * @return A BSON document representing the aggregation operation.
 */
infix fun MongoAggOperator.from(expression: Any): Bson = toString().from(expression)
