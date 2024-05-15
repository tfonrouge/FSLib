package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ListState<@Suppress("unused") T : Any>(
    val data: String = "[]",
    val last_page: Int? = null,
    val last_row: Int? = null,
    val state: String? = null,
)

@Suppress("unused")
inline fun <reified T : Any> listState(
    data: List<T>,
    last_page: Int? = null,
    last_row: Int? = null,
    state: String? = null,
): ListState<T> {
    return ListState(
        data = Json.encodeToString(data),
        last_page = last_page,
        last_row = last_row,
        state = state
    )
}

@Suppress("unused")
inline fun <reified T : Any> ListState<T>.getList(): List<T> {
    return Json.decodeFromString(data)
}

@Suppress("unused")
inline fun <reified T : Any> ListState<T>.setList(list: List<T>): ListState<T> {
    return this.copy(data = Json.encodeToString(list))
}
