package com.fonrouge.fsLib.apiServices

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeoutException

inline fun <reified PAR> serialize(value: PAR): String {
    return Json.encodeToString(value)
}

@ExperimentalGetImage
fun <A : IApiService> A.urlString(): String =
    this::class.simpleName?.let { apiServiceName ->
        val callerFuncName = Thread.currentThread().stackTrace[3].methodName
        "${AppApi.urlBase}/${AppApi.appRoute}/$apiServiceName/${callerFuncName}"
    } ?: throw Exception("Error")

@Suppress("unused")
suspend inline fun <A : IApiService, reified RET : Any> A.call(): RET {
    return remoteCall(emptyList())
}

@Suppress("unused")
suspend inline fun <A : IApiService, reified PAR1, reified RET : Any> A.call(p1: PAR1): RET {
    val s1 = serialize(p1)
    return remoteCall(listOf(s1))
}

@Suppress("unused")
suspend inline fun <A : IApiService, reified PAR1, reified PAR2, reified RET : Any> A.call(
    p1: PAR1,
    p2: PAR2
): RET {
    val s1 = serialize(p1)
    val s2 = serialize(p2)
    return remoteCall(listOf(s1, s2))
}

@ExperimentalGetImage
@Suppress("unused")
suspend inline fun <A : IApiService, reified RET : Any> A.remoteCall(params: List<String?>): RET {
    val urlString = urlString()
    val tag = "HttpClient"
    val response = try {
        Log.d("API CALL Url", urlString)
        Log.d("API CALL Type", "${RET::class.simpleName}")
        AppApi.client.post(urlString) {
            contentType(ContentType.Application.Json)
            setBody(params)
        }
    } catch (e: ClientRequestException) {
        Log.d(tag, "ClientRequestException ${e.message}")
        e.printStackTrace()
        throw e
    } catch (e: ServerResponseException) {
        Log.d(tag, "ServerResponseException ${e.message}")
        e.printStackTrace()
        throw e
    } catch (e: TimeoutException) {
        Log.d(tag, "TimeoutException ${e.message}")
        e.printStackTrace()
        throw e
    } catch (e: ConnectTimeoutException) {
        Log.d(tag, "ConnectTimeoutException ${e.message}")
        e.printStackTrace()
        throw e
    } catch (e: Exception) {
        Log.d(tag, "Url: $urlString , error: ${e.message}")
        e.printStackTrace()
        throw e
    }
    if (response.status.isSuccess()) {
        val item: RET = try {
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return item
    }
    throw Exception(response.status.toString())
}
