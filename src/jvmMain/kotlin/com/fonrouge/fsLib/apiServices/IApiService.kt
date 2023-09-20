package com.fonrouge.fsLib.apiServices

import io.ktor.server.application.*

actual interface IApiService {
    val call: ApplicationCall
}