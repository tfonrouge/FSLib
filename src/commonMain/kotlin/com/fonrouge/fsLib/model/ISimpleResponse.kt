package com.fonrouge.fsLib.model

interface ISimpleResponse {
    var isOk: Boolean
    var msgOk: String?
    var msgError: String?
    var data: String?
}
