package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.ContextDataUrl
import com.fonrouge.fsLib.model.base.BaseDoc
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import kotlin.reflect.KProperty1

/**
 * Constructs a Bson match using a property field and the [ContextDataUrl.contextId] info
 */
@Suppress("unused")
inline fun <reified U> ContextDataUrl.matchWithContextId(detailToMasterField: KProperty1<out BaseDoc<*>, U>): Bson {
    return detailToMasterField eq contextIdValue()
}
