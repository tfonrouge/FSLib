package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import org.litote.kmongo.coroutine.CoroutineCollection

@Suppress("unused")
suspend fun <T : BaseDoc<ID>, ID : Any> CoroutineCollection<T>.findById(id: ID): T? {
    return findOneById(id)
}
