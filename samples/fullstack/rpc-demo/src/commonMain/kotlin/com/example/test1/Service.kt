package com.example.test1

import dev.kilua.rpc.annotations.RpcBindingRoute
import dev.kilua.rpc.annotations.RpcService

@RpcService
interface IPingService {
    @RpcBindingRoute("IPingService.ping")
    suspend fun ping(message: String): String
}
