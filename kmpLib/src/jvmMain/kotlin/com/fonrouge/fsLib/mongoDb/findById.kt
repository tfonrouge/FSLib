package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Finds a document by its identifier within a coroutine collection.
 *
 * @param id The identifier of the document to find.
 * @return The document if found, otherwise null.
 */
@Suppress("unused")
suspend fun <T : BaseDoc<ID>, ID : Any> CoroutineCollection<T>.findById(id: ID): T? {
    return findOneById(id)
}
