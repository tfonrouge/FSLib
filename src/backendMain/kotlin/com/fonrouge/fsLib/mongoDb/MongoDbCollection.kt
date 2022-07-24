package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.annotations.Collection
import com.fonrouge.fsLib.model.base.BaseModel
import org.litote.kmongo.coroutine.coroutine
import kotlin.reflect.full.findAnnotation

@Suppress("unused")
inline fun <reified T : BaseModel<*>> mongoDbCollection(
    lookupBuilderList: List<LookupBuilder<T, *, *>>? = null,
    noinline init: (CTableDb<T>.() -> Unit)? = null
): CTableDb<T> {
    val collName: String = T::class.findAnnotation<Collection>()?.name ?: T::class.simpleName!!
    val collection = mongoDatabase.getCollection(collName, T::class.java).coroutine
    val cTableDb = CTableDb(
        collection = collection,
        lookupBuilderList = lookupBuilderList
    )
    init?.invoke(cTableDb)
    return cTableDb
}
