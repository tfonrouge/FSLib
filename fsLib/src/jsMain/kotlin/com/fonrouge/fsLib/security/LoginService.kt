package com.fonrouge.fsLib.security

import dev.kilua.rpc.SecurityException
import io.kvision.rest.HttpMethod
import io.kvision.rest.ResponseBodyType
import io.kvision.rest.RestClient
import io.kvision.rest.requestDynamic
import io.kvision.utils.obj
import kotlinx.coroutines.asDeferred

/**
 * Form login dispatcher.
 */
class LoginService(val loginEndpoint: String) {
    val loginAgent = RestClient()

    /**
     * Login with a form.
     * @param credentials username and password credentials
     */
    suspend fun login(credentials: Credentials?): Boolean =
        if (credentials?.username != null) {
            loginAgent.requestDynamic(loginEndpoint) {
                data = obj {
                    this.username = credentials.username
                    this.password = credentials.password
                }
                method = HttpMethod.POST
                contentType = "application/x-www-form-urlencoded"
                responseBodyType = ResponseBodyType.READABLE_STREAM
            }.then { _: dynamic -> true }.asDeferred().await()
        } else {
            throw SecurityException("Credentials cannot be empty")
        }
}
