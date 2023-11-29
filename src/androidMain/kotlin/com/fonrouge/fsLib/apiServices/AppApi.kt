package com.fonrouge.fsLib.apiServices

import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("unused")
object AppApi {
    var version: String = "0.0"
    var urlBase: String = "localhost"
    var appRoute: String = "appRoute"
    var userAgent: String = "AppAndroid"
    var serializedISysUser: String? = null
    private var _httpClient: HttpClient? = null
    val client: HttpClient
        get() {
            if (_httpClient == null) {
                _httpClient = HttpClient(CIO) {
                    install(Auth)
                    install(ContentNegotiation) {
                        json()
                    }
                    install(UserAgent) {
                        agent = userAgent
                    }
                    install(HttpCookies)
                    install(DefaultRequest) {
                        contentType(ContentType.Application.Json)
                    }
                    install(Logging) {
                        logger = Logger.ANDROID
                        level = LogLevel.ALL
                        sanitizeHeader { header -> header == HttpHeaders.Authorization }
                    }
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                        connectTimeoutMillis = 30000
                        socketTimeoutMillis = 30000
                    }
                }
            }
            return _httpClient!!
        }

    val logged get() = serializedISysUser != null

    fun clearHttpClient() {
        _httpClient?.close()
        _httpClient = null
    }

    inline fun <reified T : ISysUser> getISysUser(): T? {
        return serializedISysUser?.let { Json.decodeFromString(it) }
    }

    suspend inline fun <reified T : ISysUser> loginForm(loginUrl: String, userLogin: UserLogin): ItemState<T> {
        serializedISysUser = null
        val httpResponse = try {
            client.submitForm(
                url = "$urlBase/$loginUrl",
                formParameters = parameters {
                    append(UserLogin::username.name, userLogin.username)
                    append(UserLogin::password.name, userLogin.password)
                }
            )
        } catch (e: Exception) {
            return ItemState(isOk = false, msgError = e.message)
        }
        val itemState = try {
            ItemState(item = httpResponse.body<T>()).also {
                serializedISysUser = Json.encodeToString(it.item)
            }
        } catch (e: Exception) {
            ItemState(isOk = false, msgError = e.message)
        }
        return itemState
    }

    suspend fun logout(logoutUrl: String = "/logout"): SimpleState {
        serializedISysUser = null
        return try {
            client.get("$urlBase/$logoutUrl")
            SimpleState(isOk = true)
        } catch (e: Exception) {
            e.printStackTrace()
            SimpleState(isOk = false, msgError = e.message)
        }
    }
}
