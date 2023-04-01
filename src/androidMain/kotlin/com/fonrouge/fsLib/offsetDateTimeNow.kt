package com.fonrouge.fsLib

import android.annotation.TargetApi
import android.os.Build
import java.time.OffsetDateTime
import java.time.ZoneId

@TargetApi(Build.VERSION_CODES.O)
actual fun offsetDateTimeNow(): OffsetDateTime = OffsetDateTime.now(ZoneId.systemDefault())
