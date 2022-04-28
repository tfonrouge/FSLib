package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.apiLib.KVWebManager.API_SERVER

object Api {

    const val uploadService = "uploadService"
    private const val apiVersion = "api_v1.0"

    val API_BASE_URL get() = "${API_SERVER}/$apiVersion/"
}
