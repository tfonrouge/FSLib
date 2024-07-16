package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.expr
import org.litote.kmongo.from
import org.litote.kmongo.lookup
import org.litote.kmongo.match
import org.litote.kmongo.path
import org.litote.kmongo.variableDefinition
import kotlin.reflect.KProperty

/**
 * Correlated Subqueries with Concise Syntax (MongoDB 5.0)
 *
 * @param `as` is a [KProperty] reference field
 */
fun lookup5(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultField: KProperty<Any?>,
    pipeline: List<Bson>
): Bson = lookup5(
    from = from,
    localField = localField,
    foreignField = foreignField,
    let = let,
    resultField = resultField.path(),
    pipeline = pipeline
)

/**
 * Correlated Subqueries with Concise Syntax (MongoDB 5.0)
 *
 * @param `as` is a [String] field name
 */
fun lookup5(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultField: String,
    pipeline: List<Bson>
): Bson {
    val validVarName =
        (if (localField.name[0] == '_') localField.name.substring(1) else localField.name)
            .replace('.', '_')
            .let {
                if (it[0].isUpperCase())
                    it[0].lowercase() + it.substring(1)
                else
                    it
            }
    val let1: List<Variable<out Any>> =
        listOf(localField.variableDefinition(validVarName)) + (let ?: emptyList())
    val pipeline1 = mutableListOf(
        match(
            expr(
                MongoOperator.eq from listOf(
                    foreignField,
                    "\$\$$validVarName"
                )
            )
        )
    )
    pipeline1.addAll(pipeline)
    @Suppress("UNCHECKED_CAST")
    return Aggregates.lookup(
        from,
        let1 as? List<Variable<Any>>,
        pipeline1,
        resultField
    )
}

fun lookup(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    resultField: KProperty<Any?>,
): Bson = lookup(
    from = from,
    localField = localField.path(),
    foreignField = foreignField.path(),
    newAs = resultField.path()
)
