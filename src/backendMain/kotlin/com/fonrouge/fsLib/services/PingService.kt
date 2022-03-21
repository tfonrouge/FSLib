package com.fonrouge.fsLib.services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual class PingService : IPingService {
    override suspend fun ping(message: String): String {
        return "${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)} Hello from server ..."
    }
}
