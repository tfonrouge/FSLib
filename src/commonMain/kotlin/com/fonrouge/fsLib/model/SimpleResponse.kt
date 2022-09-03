package com.fonrouge.fsLib.model

import kotlinx.serialization.Serializable

@Serializable
open class SimpleResponse(
    override var isOk: Boolean,
    override var msgOk: String? = null,
    override var msgError: String? = null,
    override var data: String? = null,
) : ISimpleResponse
