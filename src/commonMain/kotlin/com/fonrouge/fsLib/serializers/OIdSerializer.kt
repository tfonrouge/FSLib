package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer

expect object OIdSerializer : KSerializer<OId<Any>>
