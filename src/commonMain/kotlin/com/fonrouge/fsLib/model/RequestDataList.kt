package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestDataList<T>(
    val data: List<T>,
    val last_page: Int,
    val last_row: Int?,
    val checksum: Long = 0,
    val changedList: Boolean,
)
