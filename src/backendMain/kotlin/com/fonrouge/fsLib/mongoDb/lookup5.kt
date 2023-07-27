package com.fonrouge.fsLib.mongoDb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import kotlin.reflect.KProperty

fun lookup(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultProperty: KProperty<Any?>,
    vararg pipeline: Bson
): Bson = lookup(
    from = from,
    localField = localField,
    foreignField = foreignField,
    let = let,
    resultProperty = resultProperty.path(),
    pipeline = pipeline
)

fun lookup(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultProperty: String,
    vararg pipeline: Bson
): Bson {
    val validVarName =
        (if (localField.name[0] == '_') localField.name.substring(1) else localField.name).replace('.', '_')
    val let1: List<Variable<out Any>> = listOf(localField.variableDefinition(validVarName)) + (let ?: emptyList())
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
        resultProperty
    )
}
