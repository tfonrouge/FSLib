package com.example.greeting

import dev.kilua.rpc.applyRoutes
import dev.kilua.rpc.getAllServiceManagers
import dev.kilua.rpc.initRpc
import dev.kilua.rpc.registerService
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.kvision.remote.registerRemoteTypes

/**
 * Ktor application module for the greeting sample.
 */
fun Application.main() {
    registerRemoteTypes()
    install(Compression)
    install(WebSockets)
    routing {
        getAllServiceManagers().forEach { applyRoutes(it) }
    }
    initRpc {
        registerService<IGreetingService> { GreetingService() }
    }
}
