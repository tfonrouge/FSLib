package com.fonrouge.backendLib.mongoDb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Variable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import kotlin.reflect.KProperty

/**
 * Performs a correlated subquery with concise syntax in MongoDB 5.0.
 *
 * @param from The collection from which to perform the lookup.
 * @param localField The field from the local collection.
 * @param foreignField The field from the foreign collection.
 * @param let Optional variables to use in the lookup pipeline.
 * @param resultField The field in the output document to store the results.
 * @param pipeline A list of aggregation stages to apply to the foreign collection.
 * @return A Bson object representing the lookup operation.
 */
fun lookup5(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultField: KProperty<Any?>,
    pipeline: List<Bson>,
): Bson = lookup5(
    from = from,
    localField = localField,
    foreignField = foreignField,
    let = let,
    resultField = resultField.path(),
    pipeline = pipeline
)

/**
 * Performs a lookup operation for MongoDB aggregation framework.
 *
 * @param from the collection to join with.
 * @param localField the field from the input documents, usually the foreign key.
 * @param foreignField the field from the documents of the `from` collection.
 * @param let the optional variables used in the `pipeline` stages.
 * @param resultField the name of the new array field to add to the input documents.
 * @param pipeline the list of aggregation stages to apply to the `from` collection.
 * @return a `Bson` object representing the lookup operation.
 */
fun lookup5(
    from: String,
    localField: KProperty<Any?>,
    foreignField: KProperty<Any?>,
    let: List<Variable<out Any>>? = null,
    resultField: String,
    pipeline: List<Bson>,
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
                    "$$$validVarName"
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

/**
 * Performs a lookup operation to join data from two collections.
 *
 * @param from the name of the foreign collection to join with.
 * @param localField the field from the local document to match values against.
 * @param foreignField the field from the foreign collection to match values with.
 * @param resultField the field to hold the result of the join operation.
 * @return a Bson object representing the lookup operation.
 */
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

/**
 * Creates a MongoDB aggregation lookup stage that joins documents from another collection into the current aggregation.
 *
 * @param from The name of the collection in the same database to perform the join with.
 * @param let Optional list of variables that can be accessed during the pipeline execution.
 * @param pipeline A list of aggregation stages applied to the documents being joined.
 * @param resultField The property representing the field where the resulting array of matched documents will be stored.
 * @return A BSON stage representing the lookup operation.
 */
@Suppress("UNCHECKED_CAST")
fun lookup(
    from: String,
    let: List<Variable<out Any>>? = null,
    pipeline: List<Bson>,
    resultField: KProperty<Any?>,
): Bson = Aggregates.lookup(
    from,
    let as? List<Variable<Any>>,
    pipeline,
    resultField.path()
)