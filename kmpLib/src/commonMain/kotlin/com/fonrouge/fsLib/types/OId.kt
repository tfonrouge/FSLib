package com.fonrouge.fsLib.types

import com.fonrouge.fsLib.objectIdHexString
import com.fonrouge.fsLib.serializers.OIdSerializer
import kotlinx.serialization.Serializable

@Serializable(with = OIdSerializer::class)
data class OId<@Suppress("unused") T>(
    val id: String = objectIdHexString(),
)