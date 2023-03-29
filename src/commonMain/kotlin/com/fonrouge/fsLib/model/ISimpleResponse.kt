package com.fonrouge.fsLib.model

interface ISimpleResponse {
    val isOk: Boolean
    val msgOk: String?
    val msgError: String?
    val state: String?
}
