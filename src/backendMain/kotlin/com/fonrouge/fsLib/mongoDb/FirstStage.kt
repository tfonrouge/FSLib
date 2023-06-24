package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson

@Suppress("unused")
class FirstStage(
    val pipeline: MutableList<Bson>,
    val count: Long,
    val last_page: Int,
    val last_row: Int?,
    val limit: Int?,
)
