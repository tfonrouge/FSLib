package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson

data class ListFirstStage(
    val pipeline: MutableList<Bson> = mutableListOf(),
    val pageSize: Int?,
    val page: Int?,
//    var count: Long? = null,
    val preLookupMatch: Bson? = null,
    val postLookupMatch: Bson? = null,
    val preLookupSort: Bson? = null,
    val postLookupSort: Bson? = null,
)
