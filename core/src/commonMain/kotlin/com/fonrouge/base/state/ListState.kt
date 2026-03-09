package com.fonrouge.base.state

import com.fonrouge.base.offsetDateTimeNow
import com.fonrouge.base.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * This data class represents the state of a list.
 *
 * @param T The type of elements in the list.
 * @property data JSON representation of the list data.
 * @property last_page The index of the last page in a paginated list.
 * @property last_row The index of the last row in the list.
 * @property state The current state of the list, conforming to the `State` enum.
 * @property msgOk The success message.
 * @property msgError The error message.
 * @property dateTime The date and time when the state was last modified.
 * @property hasError Indicates whether the current state represents an error.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ListState<T : Any>(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val data: List<T> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val last_page: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val last_row: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    override val state: State = State.Ok,
    override val msgOk: String? = MSG_OK,
    override val msgError: String? = MSG_ERROR,
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()
    override val hasError: Boolean get() = state == State.Error
}

/**
 * Converts a list of data items to a ListState object with additional state information.
 *
 * @param T the type of elements in the data list.
 * @param data the list of data items.
 * @param last_page the last page number, optional.
 * @param last_row the last row number, optional.
 * @param state the state of the list, defaults to State.Ok.
 * @return a ListState object containing the serialized data and additional state information.
 */
@Suppress("unused")
inline fun <reified T : Any> listState(
    data: List<T>,
    last_page: Int? = null,
    last_row: Int? = null,
    state: State = State.Ok,
): ListState<T> {
    return ListState(
        data = data,
        last_page = last_page,
        last_row = last_row,
        state = state,
        msgOk = MSG_OK,
        msgError = MSG_ERROR,
    )
}

/**
 * Updates the current `ListState` with the provided list and returns a new `ListState` instance.
 *
 * @param T The type of elements in the list.
 * @param list The new list to set in the `ListState`.
 * @return A new `ListState` instance with the updated list data.
 */
@Suppress("unused")
inline fun <reified T : Any> ListState<T>.setList(list: List<T>): ListState<T> = this.copy(data = list)
