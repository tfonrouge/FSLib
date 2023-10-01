package com.fonrouge.fsLib.apiServices

import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

@Suppress("unused")
object AppApi {
    var version: String = "0.0"
    var urlBase: String = "localhost"
    var appRoute: String = "appRoute"
    var userAgent: String = "AppAndroid"
    var username: String? = null
    private var jwtToken: JwtToken? = null
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(Auth) {
                bearer {
                    refreshTokens {
                        jwtToken?.let {
                            BearerTokens(accessToken = it.token, it.token)
                        }
                    }
                }
            }
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
        }
    }


    val logged: Boolean
        get() {
            return jwtToken != null
        }

    suspend fun login(userLogin: UserLogin): SimpleState {
        jwtToken = null
        val httpResponse = try {
            client.post("$urlBase/jwtLogin") {
                setBody(userLogin)
            }
        } catch (e: Exception) {
            return SimpleState(isOk = false, e.message)
        }
        val itemState = try {
            httpResponse.body<ItemState<JwtToken>>()
        } catch (e: Exception) {
            return SimpleState(isOk = false, e.message)
        }
        jwtToken = itemState.item
        return jwtToken?.token?.let {
            username = userLogin.username
            SimpleState(isOk = true)
        } ?: SimpleState(isOk = false, msgError = itemState.msgError)
    }

    suspend fun logout() {
        try {
            val logoutResponse = client.get("$urlBase/logout") {
//                jwtToken?.token?.let { bearerAuth(it) }
            }
            println(logoutResponse)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
