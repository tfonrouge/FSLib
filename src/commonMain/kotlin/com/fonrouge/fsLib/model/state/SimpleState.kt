package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
data class SimpleState(
    override var isOk: Boolean,
    override var msgOk: String? = null,
    override var msgError: String? = null,
) : ISimpleState
