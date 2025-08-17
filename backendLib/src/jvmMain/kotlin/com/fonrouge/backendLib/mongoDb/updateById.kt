package com.fonrouge.backendLib.mongoDb

import com.fonrouge.fsLib.model.BaseDoc
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.util.UpdateConfiguration

/**
 * Updates a document in the collection by its ID.
 *
 * @param id The ID of the document to be updated.
 * @param update The update operations to be applied to the document.
 * @param options Optional update options to control the update behavior.
 * @param updateOnlyNotNullProperties If true, only non-null properties in the update object will be applied.
 * @return The result of the update operation.
 */
@Suppress("unused")
suspend fun <T : BaseDoc<ID>, ID : Any> CoroutineCollection<T>.updateById(
    id: ID,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties,
): UpdateResult {
    return updateOneById(
        id = id,
        update = update,
        options = options,
        updateOnlyNotNullProperties = updateOnlyNotNullProperties
    )
}
