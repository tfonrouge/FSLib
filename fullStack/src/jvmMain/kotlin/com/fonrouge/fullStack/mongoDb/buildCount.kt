package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.mongodb.client.model.Field
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.addFields
import org.litote.kmongo.count
import org.litote.kmongo.expr
import org.litote.kmongo.from
import org.litote.kmongo.match
import org.litote.kmongo.rem
import org.litote.kmongo.variable
import org.litote.kmongo.variableDefinition
import kotlin.reflect.KProperty1

/**
 * Builds a count aggregation pipeline for the target collection.
 *
 * @param collForeign The foreign collection to perform the lookup aggregation on.
 * @param localKey The key field in the local collection used for matching.
 * @param foreignIdKey The key field in the foreign collection used for matching.
 * @param resultField The field in which the count result will be stored.
 * @return A configured [LookupPipelineBuilder] for performing the aggregation.
 */
@Suppress("unused", "UnusedReceiverParameter")
fun <T: BaseDoc<*>, CF : Coll<out ICommonContainer<F, FID, *>, F, FID, *, *>, F : BaseDoc<FID>, FID : Any> Coll<out ICommonContainer<T,*,*>,T,*,*,*>.buildCount(
    collForeign: CF,
    localKey: KProperty1<T, Any?>,
    foreignIdKey: KProperty1<F, Any?>,
    resultField: KProperty1<in T, Any?>
): LookupPipelineBuilder<T, F, FID> {
    val normalizedLocalKeyName = "localIdKey_${localKey.name}"
    return lookupAnyField(
        coll = collForeign,
        let = listOf(
            localKey.variableDefinition(normalizedLocalKeyName),
        ),
        pipeline = listOf(
            match(
                expr(
                    MongoOperator.eq from listOf(
                        normalizedLocalKeyName.variable,
                        foreignIdKey
                    )
                )
            ),
            resultField.count(),
        ),
        resultField = resultField,
        addStages = listOf(
            addFields(
                Field(
                    resultField.name,
                    resultField % resultField
                ),
            )
        )
    )
}
