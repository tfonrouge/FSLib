package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ListState<@Suppress("unused") T : Any>(
    val data: String = "[]",
    val last_page: Int? = null,
    val last_row: Int? = null,
    override val state: State = State.Ok,
    override val msgOk: String? = MSG_OK,
    override val msgError: String? = MSG_ERROR,
    override val cargo: String? = null,
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error
}

@Suppress("unused")
inline fun <reified T : Any> listState(
    data: List<T>,
    last_page: Int? = null,
    last_row: Int? = null,
    state: State = State.Ok,
    cargo: String? = null,
): ListState<T> {
    return ListState(
        data = Json.encodeToString(data),
        last_page = last_page,
        last_row = last_row,
        state = state,
        msgOk = MSG_OK,
        msgError = MSG_ERROR,
        cargo = cargo,
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
