package com.example.greeting

import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service for generating personalized greetings.
 */
@RpcService
interface IGreetingService {

    /**
     * Returns a personalized greeting for the given [name].
     */
    suspend fun greet(name: String): String

    /**
     * Returns the current server timestamp as a formatted string.
     */
    suspend fun serverTime(): String
}
