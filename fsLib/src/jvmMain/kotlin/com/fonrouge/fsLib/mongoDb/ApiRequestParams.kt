package com.fonrouge.fsLib.mongoDb

import org.bson.conversions.Bson

/**
 * A data class representing the first stage in a pipeline,
 * used for operations involving paginated data and sorting/matching BSON documents.
 *
 * @property pipeline A mutable list of BSON documents representing stages in the pipeline.
 * @property pageSize The number of items per page, can be null if not paginated.
 * @property page The current page number, can be null if not paginated.
 * @property postLookupMatch An optional BSON document to match documents after a lookup operation.
 * @property preLookupSort An optional BSON document to sort documents before a lookup operation.
 * @property postLookupSort An optional BSON document to sort documents after a lookup operation.
 */
data class ApiRequestParams(
    val pipeline: MutableList<Bson> = mutableListOf(),
    val pageSize: Int?,
    val page: Int?,
    val postLookupMatch: Bson? = null,
    val preLookupSort: Bson? = null,
    val postLookupSort: Bson? = null,
)
