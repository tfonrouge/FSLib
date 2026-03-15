package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.model.BaseDoc
import com.mongodb.client.model.Field
import org.bson.conversions.Bson
import org.litote.kmongo.*
import kotlin.reflect.KProperty1

/**
 * Builds a lookup pipeline to count related entities in a foreign collection and appends the result as a field in the local collection.
 *
 * @param collForeign The foreign collection to perform the lookup on.
 * @param localKey The local key in the current collection used for matching.
 * @param foreignIdKey The key in the foreign collection used for matching.
 * @param pipeline Optional stages to apply in the pipeline before the count stage.
 * @param resultField The field in the local collection to store the count result.
 * @return A configured `LookupPipelineBuilder` to execute the lookup operation.
 */
@Suppress("unused", "UnusedReceiverParameter")
fun <T : BaseDoc<*>, CF : Coll<F, FID, *, *>, F : BaseDoc<FID>, FID : Any> Coll<T, *, *, *>.buildCount(
    collForeign: CF,
    localKey: KProperty1<T, Any?>,
    foreignIdKey: KProperty1<F, Any?>,
    pipeline: List<Bson>? = null,
    resultField: KProperty1<in T, Any?>
): LookupPipelineBuilder<T, F, FID> {
    val normalizedLocalKeyName = "localIdKey_${localKey.name}".replace(".", "_")
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
            )
        ) + (pipeline ?: emptyList()) + resultField.count(),
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
