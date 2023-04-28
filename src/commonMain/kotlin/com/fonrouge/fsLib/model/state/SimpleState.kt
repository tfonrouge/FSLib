package com.fonrouge.fsLib.model.state

import kotlinx.serialization.Serializable

@Serializable
data class SimpleState(
    override var isOk: Boolean,
    override var msgOk: String? = null,
    override var msgError: String? = null,
    override var state: String? = null,
) : ISimpleState
