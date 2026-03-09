package com.example.greeting

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Server-side implementation of [IGreetingService].
 */
class GreetingService : IGreetingService {

    override suspend fun greet(name: String): String {
        val trimmed = name.trim().ifEmpty { "World" }
        return "Hello, $trimmed! Welcome to FSLib fullStack."
    }

    override suspend fun serverTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
