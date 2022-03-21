package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.apiLib.KVWebManager.API_SERVER
import com.fonrouge.fsLib.config.dataUrlPrefix
import com.fonrouge.fsLib.model.UserLogged
import com.fonrouge.fsLib.model.UserLogin
import io.kvision.rest.HttpMethod
import io.kvision.rest.RemoteRequestException
import io.kvision.rest.RestClient
import io.kvision.rest.call
import kotlinx.coroutines.await
import kotlin.js.Date
import kotlin.js.Promise

object Api {

    const val uploadService = "uploadService"
    private const val apiVersion = "api_v1.0"

    val API_BASE_URL get() = "${API_SERVER}/$apiVersion/"

    val restClient = RestClient()

    private fun headerAuthRequest(): Pair<String, String>? {
        return KVWebManager.getJwtToken()?.let {
            Pair("Authorization", "Bearer $it")
        }
    }

    fun headers(auth: Boolean): () -> List<Pair<String, String>> {
        val list = ArrayList<Pair<String, String>>()
        if (auth) {
            headerAuthRequest()?.let { list.add(it) }
        }
        return { list }
    }

    suspend fun login(userName: String?, password: String?): UserLogged? {
        return restCall(
            url = "$API_BASE_URL$dataUrlPrefix/User/login",
            data = UserLogin(
                username = userName,
                password = password
            ),
            method = HttpMethod.POST,
            headers = headers(true)
        )
    }

    suspend fun loadUser(): UserLogged? {
        return restCall(
            url = "${API_BASE_URL}validateUser",
            method = HttpMethod.GET,
            headers = headers(auth = true)
        )
    }

    suspend inline fun <reified T : Any> restCall(
        url: String,
        method: HttpMethod,
        noinline headers: (() -> List<Pair<String, String>>)? = null,
        debug: Boolean = true,
    ): T? {
        var time: Int? = null
        if (debug) {
            console.info("< restClient.call - url:", url, ", method:", method)
            time = Date().getMilliseconds()
        }
        return waitRestResponse(
            promise = restClient.call(
                url = url,
            ) {
                this.method = method
                this.headers = headers
            },
            time = time,
            debug = debug
        )
    }

    suspend inline fun <reified T : Any, reified V : Any> restCall(
        url: String,
        data: V,
        method: HttpMethod,
        noinline headers: (() -> List<Pair<String, String>>)? = null,
        debug: Boolean = true,
        noinline block: ((T?) -> Unit)? = null,
    ): T? {
        var time: Int? = null
        if (debug) {
            console.info("< restClient.call - url:", url, ", method:", method, ", with data:", data.toString())
            time = Date().getMilliseconds()
        }
        return waitRestResponse(
            promise = restClient.call(
                url = url,
                data = data,
            ) {
                this.method = method
                this.headers = headers
            },
            time = time,
            debug = debug,
            block = block
        )
    }

    suspend fun <T> waitRestResponse(
        promise: Promise<T>,
        time: Int?,
        debug: Boolean,
        block: ((T?) -> Unit)? = null,
    ): T? {
        return try {
            val result = promise.await()
            block?.let { it(result) }
            promise.await().also {
                if (debug && time != null) {
                    console.info("> restClient.call - ms:", Date().getMilliseconds() - time, ", result:", it)
                }
            }
        } catch (e: RemoteRequestException) {
            console.error("restClientCall RemoteRequestException:", e.code, e)
            if (e.code.toInt() == 403) {
                console.error("Error 403 found !!!", e.code, e)
            }
            KVWebManager.showToastApiRemoteRequest(
                code = e.code.toInt(),
                title = "Api error: ${e.code} - ${e.message}",
                message = """
<p><p>
Method: ${e.method}<br>
Timestamp: ${Date().toLocaleString()}<br>
Url: ${e.url}<br>
                """.trimIndent()
            )

            null
        } catch (e: Exception) {
            console.error("restClientCall Exception:", e)
            KVWebManager.showToastApiError(
                title = "Api error",
                message = e.message ?: "?"
            )
            null
        }
    }

    suspend fun settings(
        image: String?,
        username: String?,
        bio: String?,
        email: String?,
        password: String?,
    ): UserLogged? {
        return restCall(
            url = "${API_BASE_URL}User",
            data = UserLogged(
                id = "*",
                image = image,
                userId = username,
                fullName = bio,
                email = email,
                password = password
            ),
            method = HttpMethod.PUT,
            headers = headers(auth = true)
        )
    }
}
