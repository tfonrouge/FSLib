package com.fonrouge.fsLib.serializers

import kotlinx.serialization.KSerializer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object FSNumberInt32Serializer : KSerializer<Int>