package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer

expect object FSObjectIdSerializer : KSerializer<String>
