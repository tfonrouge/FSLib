package com.fonrouge.fsLib.model.state

import com.fonrouge.fsLib.offsetDateTimeNow
import com.fonrouge.fsLib.serializers.FSOffsetDateTimeSerializer
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SimpleState(
    override var isOk: Boolean,
    override var msgOk: String? = null,
    override var msgError: String? = null,
    override val state: String? = null,
) : ISimpleState {
    @Serializable(with = FSOffsetDateTimeSerializer::class)
    override val dateTime: OffsetDateTime = offsetDateTimeNow()

    @Suppress("unused")
    constructor(itemState: ItemState<*>) : this(
        isOk = itemState.isOk,
        msgOk = itemState.msgOk,
        msgError = itemState.msgError
    )
}
