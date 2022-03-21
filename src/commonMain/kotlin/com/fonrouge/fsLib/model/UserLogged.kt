@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.model.base.BaseModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
class UserLogged(
    override var id: String,
    var fullName: String? = null,
    var userId: String? = null,
    var password: String? = null,
    var userLevel: String? = null,
    var token: String? = null,
    var email: String? = null,
    var image: String? = null,
    var lastLogin: Instant? = null,
) : BaseModel<String>() {
//    override var upsertInfo: UpsertInfo? = null
}

@Serializable
class UserLogin(
    val username: String? = null,
    val password: String? = null,
)
