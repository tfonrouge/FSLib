@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fonrouge.fslib.model.base

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
class UpsertInfo(
    var userId: String,
    var userName: String,
    var userKey: String,
    var date: String,
    var type: String,
)
