package com.fonrouge.fsLib.services

actual class PingService : IPingService {
    override suspend fun ping(message: String): String {
        println("message from frontend: $message")
        return "hello from server..."
    }
}
