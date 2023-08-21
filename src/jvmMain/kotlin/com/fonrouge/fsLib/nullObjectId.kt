package com.fonrouge.fsLib

import com.fonrouge.fsLib.serializers.OId

@Suppress("unused")
fun <T> nullOId(): OId<T> = OId("0".repeat(24))
