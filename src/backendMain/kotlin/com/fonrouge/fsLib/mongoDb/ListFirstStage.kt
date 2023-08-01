package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson

@Suppress("unused")
data class ListFirstStage(
    val pipeline: MutableList<Bson>,
    val pageSize: Int,
    val page: Int,
//    var count: Long? = null,
    val preLookupMatch: Bson? = null,
    val postLookupMatch: Bson? = null,
    val sort: Bson? = null,
)
