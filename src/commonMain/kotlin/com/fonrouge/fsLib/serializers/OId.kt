package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.objectIdHexString
import kotlinx.serialization.Serializable

@Serializable(with = OIdSerializer::class)
data class OId<@Suppress("unused") T>(
    val id: String? = objectIdHexString(),
)
