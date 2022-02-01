@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fslib.model

import com.fonrouge.fslib.model.base.BaseModel
import com.fonrouge.fslib.model.base.UpsertInfo
import io.kvision.types.LocalDateTime
import kotlinx.serialization.Contextual
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
    @Contextual
    var lastLogin: LocalDateTime? = null,
) : BaseModel<String>() {
//    override var upsertInfo: UpsertInfo = UpsertInfo("","", "", "", "")
}

@Serializable
class UserLogin(
    val username: String? = null,
    val password: String? = null,
)
