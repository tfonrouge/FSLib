package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.StateList
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import kotlin.reflect.KProperty1

/**
 * Constructs a Bson match using a property field and the [StateList.contextId] info
 */
@Suppress("unused")
inline fun <reified U> StateList.matchWithContextId(detailToMasterField: KProperty1<out BaseDoc<*>, U>): Bson {
    return detailToMasterField eq contextIdValue()
}
