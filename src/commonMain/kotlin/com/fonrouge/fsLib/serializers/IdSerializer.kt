package com.fonrouge.fsLib.serializers

import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.serialization.KSerializer

expect object IdSerializer : KSerializer<Id<out BaseModel<*>>>
