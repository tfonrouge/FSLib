package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.util.UpdateConfiguration

@Suppress("unused")
suspend fun <T : BaseDoc<ID>, ID : Any> CoroutineCollection<T>.updateById(
    id: ID,
    update: Any,
    options: UpdateOptions = UpdateOptions(),
    updateOnlyNotNullProperties: Boolean = UpdateConfiguration.updateOnlyNotNullProperties
): UpdateResult {
    return updateOneById(
        id = id,
        update = update,
        options = options,
        updateOnlyNotNullProperties = updateOnlyNotNullProperties
    )
}
