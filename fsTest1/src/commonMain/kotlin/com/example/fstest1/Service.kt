package com.example.fstest1

import dev.kilua.rpc.annotations.RpcService

@RpcService
interface IPingService {
    suspend fun ping(message: String): String
}
