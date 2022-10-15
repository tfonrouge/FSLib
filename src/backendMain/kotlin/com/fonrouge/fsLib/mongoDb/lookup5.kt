package com.fonrouge.fsLib.mongoDb

import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson

fun lookup5(
    from: String,
    localField: String,
    foreignField: String,
    let: Document? = null,
    pipeline: List<Bson>? = null,
    newAs: String,
): Bson {
    val document = Document()
        .append("from", from)
        .append("localField", localField)
        .append("foreignField", foreignField)
    let?.let { document.append("let", it) }
    pipeline?.let {
        if (pipeline.isNotEmpty()) document.append("pipeline", pipeline)
    }
    document.append("as", newAs).toBsonDocument()
    return BsonDocument("\$lookup", document.toBsonDocument())
}

