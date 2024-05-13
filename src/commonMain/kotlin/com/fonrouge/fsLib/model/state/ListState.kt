package com.fonrouge.fsLib.model.state

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ListState<T : Any>(
    val data: String = "[]",
    val last_page: Int? = null,
    val last_row: Int? = null,
    var contentHashCode: Int? = null,
    val state: String? = null,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @Transient
    var list: List<T> = emptyList()
}
