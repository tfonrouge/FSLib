package com.fonrouge.backendLib.mongoDb

import com.fonrouge.base.model.BaseDoc
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Finds a document by its identifier within a coroutine collection.
 *
 * @param id The identifier of the document to find.
 * @return The document if found, otherwise null.
 */
suspend fun <T : BaseDoc<ID>, ID : Any> CoroutineCollection<T>.findById(id: ID): T? {
    return findOneById(id)
}
