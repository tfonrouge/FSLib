package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.MongoDoc
import com.fonrouge.fsLib.model.base.BaseModel
import org.litote.kmongo.coroutine.coroutine
import kotlin.reflect.full.findAnnotation

@Suppress("unused")
inline fun <reified T : BaseModel<U>, U : Any> mongoDbCollection(
    lookupBuilderList: List<LookupBuilder<T, *, *, *>>? = null,
    noinline init: (CTableDb<T, U>.() -> Unit)? = null
): CTableDb<T, U> {
    val collName: String = T::class.findAnnotation<MongoDoc>()?.collection ?: T::class.simpleName!!
    val collection = mongoDatabase.getCollection(collName, T::class.java).coroutine
    val cTableDb = CTableDb(
        collection = collection,
        lookupBuilderList = lookupBuilderList
    )
    init?.invoke(cTableDb)
    return cTableDb
}
