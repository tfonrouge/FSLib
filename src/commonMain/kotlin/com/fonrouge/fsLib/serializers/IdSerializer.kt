package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer

expect object IdSerializer : KSerializer<Id<Any>>
